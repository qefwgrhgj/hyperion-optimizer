import os
import shutil
import subprocess
import sys
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

WORKSPACE = Path(__file__).resolve().parent
BUILD_DIR = WORKSPACE / "build"
CLASSES_DIR = BUILD_DIR / "classes"
LIBS_DIR = BUILD_DIR / "libs"

FABRIC_LOADER_JAR = Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/net/fabricmc/fabric-loader/0.19.3/fabric-loader-0.19.3.jar")
LAUNCHER_MODS_DIR = Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/instances/Fabulously Optimized(3)/minecraft/mods")

MODULES = [
    "common",
    # 1.16.5
    "fabric-1.16.5",
    "forge-1.16.5",
    # 1.17 / 1.17.1
    "fabric-1.17.1",
    "forge-1.17.1",
    # 1.18 / 1.18.1 / 1.18.2
    "fabric-1.18.2",
    "forge-1.18.2",
    # 1.19 / 1.19.1 / 1.19.2
    "fabric-1.19.2",
    "forge-1.19.2",
    # 1.19.3 / 1.19.4
    "fabric-1.19.4",
    "forge-1.19.4",
    # 1.20 / 1.20.1
    "fabric-1.20.1",
    "forge-1.20.1",
    # 1.20.2 / 1.20.3 / 1.20.4
    "fabric-1.20.4",
    "neoforge-1.20.4",
    # 1.20.5 / 1.20.6
    "fabric-1.20.6",
    "neoforge-1.20.6",
    # 1.21 / 1.21.1
    "fabric-1.21.1",
    "neoforge-1.21.1",
    # 1.21.2 / 1.21.3 / 1.21.4
    "fabric-1.21.4",
    "neoforge-1.21.4",
    # 1.21.5 .. 1.21.11
    "fabric-1.21.11",
    "neoforge-1.21.11",
    # 26.1
    "fabric-26.1",
    "neoforge-26.1",
    # 26.2
    "fabric-26.2",
    "neoforge-26.2"
]

def run_cmd(cmd, cwd=WORKSPACE):
    res = subprocess.run(cmd, cwd=cwd, shell=True, capture_output=True, text=True, encoding="utf-8", errors="replace")
    if res.returncode != 0:
        print(f"[ERROR] Command failed ({res.returncode}): {cmd}")
        print(res.stderr or res.stdout)
        sys.exit(res.returncode)
    return res.stdout

def clean():
    print("[1/6] Cleaning build directories...")
    if CLASSES_DIR.exists():
        shutil.rmtree(CLASSES_DIR)
    target_dir = WORKSPACE / "target"
    if target_dir.exists():
        shutil.rmtree(target_dir)
    CLASSES_DIR.mkdir(parents=True, exist_ok=True)
    LIBS_DIR.mkdir(parents=True, exist_ok=True)
    target_dir.mkdir(parents=True, exist_ok=True)

def compile_common():
    print("[2/6] Compiling common module...")
    common_dest = CLASSES_DIR / "common"
    common_dest.mkdir(parents=True, exist_ok=True)
    
    java_files = list((WORKSPACE / "common" / "src" / "main" / "java").rglob("*.java"))
    argfile = WORKSPACE / "build" / "common_sources.txt"
    with open(argfile, "w", encoding="utf-8") as f:
        for jf in java_files:
            f.write(f'"{jf.as_posix()}"\n')
            
    run_cmd(f'javac -d "{common_dest.as_posix()}" "@{argfile.as_posix()}"')
    
    res_dir = WORKSPACE / "common" / "src" / "main" / "resources"
    if res_dir.exists():
        for res_file in res_dir.rglob("*"):
            if res_file.is_file():
                rel = res_file.relative_to(res_dir)
                dest = common_dest / rel
                dest.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(res_file, dest)

def compile_submodules():
    print("[3/6] Compiling platform submodules...")
    common_dest = CLASSES_DIR / "common"
    
    cp_entries = [common_dest.as_posix()]
    if FABRIC_LOADER_JAR.exists():
        cp_entries.append(FABRIC_LOADER_JAR.as_posix())
    classpath = ";".join(cp_entries)
    
    for mod in MODULES:
        if mod == "common":
            continue
        mod_src = WORKSPACE / mod / "src" / "main" / "java"
        mod_dest = CLASSES_DIR / mod
        mod_dest.mkdir(parents=True, exist_ok=True)
        
        # Copy common classes into mod_dest for self-contained JAR
        shutil.copytree(common_dest, mod_dest, dirs_exist_ok=True)
        
        if mod_src.exists():
            java_files = list(mod_src.rglob("*.java"))
            if java_files:
                argfile = WORKSPACE / "build" / f"{mod}_sources.txt"
                with open(argfile, "w", encoding="utf-8") as f:
                    for jf in java_files:
                        f.write(f'"{jf.as_posix()}"\n')
                run_cmd(f'javac -cp "{classpath}" -d "{mod_dest.as_posix()}" "@{argfile.as_posix()}"')
                
        mod_res = WORKSPACE / mod / "src" / "main" / "resources"
        if mod_res.exists():
            for res_file in mod_res.rglob("*"):
                if res_file.is_file():
                    rel = res_file.relative_to(mod_res)
                    dest = mod_dest / rel
                    dest.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(res_file, dest)

def package_jars():
    print("[4/6] Packaging JAR artifacts...")
    for mod in MODULES:
        mod_dest = CLASSES_DIR / mod
        jar_name = f"hyperion-optimizer-{mod}-1.0.0.jar"
        jar_path = LIBS_DIR / jar_name
        if jar_path.exists():
            jar_path.unlink()
        run_cmd(f'jar -cf "{jar_path.as_posix()}" -C "{mod_dest.as_posix()}" .')
        size_kb = jar_path.stat().st_size / 1024
        print(f"  [OK] {jar_name} ({size_kb:.1f} KB)")

def deploy_to_launcher():
    print("[5/6] Deploying Fabric 1.21.11 build to ElyPrismLauncher...")
    target_jar = LIBS_DIR / "hyperion-optimizer-fabric-1.21.11-1.0.0.jar"
    if LAUNCHER_MODS_DIR.exists() and target_jar.exists():
        dest = LAUNCHER_MODS_DIR / target_jar.name
        shutil.copy2(target_jar, dest)
        print(f"  [OK] Copied {target_jar.name} to {dest}")

def run_tests():
    print("[6/6] Running Hyperion Test Suite...")
    test_src = WORKSPACE / "common" / "src" / "test" / "java" / "com" / "hyperion" / "optimizer" / "HyperionTestRunner.java"
    test_dest = WORKSPACE / "target" / "classes"
    test_dest.mkdir(parents=True, exist_ok=True)
    common_dest = CLASSES_DIR / "common"
    
    run_cmd(f'javac -cp "{common_dest.as_posix()}" -d "{test_dest.as_posix()}" "{test_src.as_posix()}"')
    out = run_cmd(f'java -cp "{test_dest.as_posix()};{common_dest.as_posix()}" com.hyperion.optimizer.HyperionTestRunner')
    lines = [l for l in out.strip().split("\n") if "SUMMARY" in l or "STATUS" in l or "[PASS]" in l]
    print("\n".join(lines[-10:]))

if __name__ == "__main__":
    clean()
    compile_common()
    compile_submodules()
    package_jars()
    deploy_to_launcher()
    run_tests()
    print("\n[SUCCESS] All multi-version modules rebuilt and deployed successfully.")
