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
MINECRAFT_JAR = Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/com/mojang/minecraft/26.2/minecraft-26.2-client.jar")
MODMENU_JAR = Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/instances/Fabulously Optimized(2)/minecraft/mods/modmenu-14.0.0-rc.2.jar")
BRIGADIER_JAR = Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/com/mojang/brigadier/1.3.10/brigadier-1.3.10.jar")
SPONGE_MIXIN_JAR = Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/net/fabricmc/sponge-mixin/0.17.3+mixin.0.8.7/sponge-mixin-0.17.3+mixin.0.8.7.jar")
FORGE_MOD_JAR = Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/net/minecraftforge/javafmllanguage/1.20.1-47.4.22/javafmllanguage-1.20.1-47.4.22.jar")
NEOFORGE_MOD_JAR = Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/net/neoforged/fancymodloader/loader/11.0.13/loader-11.0.13.jar")

REQUIRED_JARS = [
    FABRIC_LOADER_JAR,
    MINECRAFT_JAR,
    BRIGADIER_JAR,
    SPONGE_MIXIN_JAR,
    FORGE_MOD_JAR,
    NEOFORGE_MOD_JAR,
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/com/mojang/datafixerupper/10.0.21/datafixerupper-10.0.21.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/com/google/code/gson/gson/2.14.0/gson-2.14.0.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/com/google/guava/guava/33.6.0-jre/guava-33.6.0-jre.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/it/unimi/dsi/fastutil/8.5.18/fastutil-8.5.18.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/org/joml/joml/1.10.8/joml-1.10.8.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/org/apache/commons/commons-lang3/3.20.0/commons-lang3-3.20.0.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/commons-io/commons-io/2.20.0/commons-io-2.20.0.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/commons-codec/commons-codec/1.22.0/commons-codec-1.22.0.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/by/ely/authlib/9.0.75-ely.1/authlib-9.0.75-ely.1.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/com/mojang/logging/1.7.12/logging-1.7.12.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/org/apache/logging/log4j/log4j-api/2.26.0/log4j-api-2.26.0.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/org/apache/logging/log4j/log4j-slf4j2-impl/2.26.0/log4j-slf4j2-impl-2.26.0.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/org/ow2/asm/asm/9.10.1/asm-9.10.1.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/org/ow2/asm/asm-tree/9.10.1/asm-tree-9.10.1.jar"),
    Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/libraries/org/ow2/asm/asm-commons/9.10.1/asm-commons-9.10.1.jar"),
]
PATH_SEP = os.pathsep
EXTRA_CP = [p.as_posix() for p in REQUIRED_JARS if p.exists()]
EXTRA_CP_STR = PATH_SEP.join(EXTRA_CP)
LAUNCHER_MODS_DIR = Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/instances/Fabulously Optimized/minecraft/mods")

JDK_BIN = Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/java/java-runtime-epsilon/bin")
JAVAC_CMD = f'"{JDK_BIN / ("javac.exe" if os.name == "nt" else "javac")}"' if (JDK_BIN / ("javac.exe" if os.name == "nt" else "javac")).exists() else (shutil.which("javac") or "javac")
JAR_CMD = f'"{JDK_BIN / ("jar.exe" if os.name == "nt" else "jar")}"' if (JDK_BIN / ("jar.exe" if os.name == "nt" else "jar")).exists() else (shutil.which("jar") or "jar")
JAVA_CMD = f'"{JDK_BIN / ("java.exe" if os.name == "nt" else "java")}"' if (JDK_BIN / ("java.exe" if os.name == "nt" else "java")).exists() else (shutil.which("java") or "java")

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

def get_module_release(mod_name):
    if "1.16.5" in mod_name:
        return "8"
    elif "1.17" in mod_name:
        return "16"
    elif any(v in mod_name for v in ["1.18", "1.19", "1.20.1", "1.20.4"]):
        return "17"
    else:
        return "21"

def compile_stubs():
    stubs_dest = CLASSES_DIR / "stubs"
    stubs_dest.mkdir(parents=True, exist_ok=True)
    stub_files = list((WORKSPACE / "compile-stubs" / "src" / "main" / "java").rglob("*.java"))
    if stub_files:
        cp_flag = f'-cp "{EXTRA_CP_STR}"' if EXTRA_CP_STR else ""
        argfile = WORKSPACE / "build" / "stub_sources.txt"
        with open(argfile, "w", encoding="utf-8") as f:
            for jf in stub_files:
                f.write(f'"{jf.as_posix()}"\n')
        run_cmd(f'{JAVAC_CMD} --release 8 {cp_flag} -d "{stubs_dest.as_posix()}" "@{argfile.as_posix()}"')

def compile_common():
    compile_stubs()
    print("[2/6] Compiling common module...")
    common_dest = CLASSES_DIR / "common"
    common_dest.mkdir(parents=True, exist_ok=True)

    stubs_dest = CLASSES_DIR / "stubs"
    full_cp = f"{stubs_dest.as_posix()}{PATH_SEP}{EXTRA_CP_STR}" if EXTRA_CP_STR else stubs_dest.as_posix()
    cp_flag = f'-cp "{full_cp}"'

    java_files = list((WORKSPACE / "common" / "src" / "main" / "java").rglob("*.java"))
    argfile = WORKSPACE / "build" / "common_sources.txt"
    with open(argfile, "w", encoding="utf-8") as f:
        for jf in java_files:
            f.write(f'"{jf.as_posix()}"\n')

    run_cmd(f'{JAVAC_CMD} --release 8 {cp_flag} -d "{common_dest.as_posix()}" "@{argfile.as_posix()}"')

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
    stubs_dest = CLASSES_DIR / "stubs"

    classpath = f"{common_dest.as_posix()}{PATH_SEP}{stubs_dest.as_posix()}{PATH_SEP}{EXTRA_CP_STR}" if EXTRA_CP_STR else f"{common_dest.as_posix()}{PATH_SEP}{stubs_dest.as_posix()}"

    for mod in MODULES:
        if mod == "common":
            continue
        rel_ver = get_module_release(mod)
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
                run_cmd(f'{JAVAC_CMD} --release {rel_ver} -cp "{classpath}" -d "{mod_dest.as_posix()}" "@{argfile.as_posix()}"')

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
        # CRITICAL: Strip compile-time stub packages (e.g. com/terraformersmc, net/minecraftforge, net/neoforged) to prevent classloader shadowing of official loaders
        for stub_pkg in ["com/terraformersmc", "net/minecraftforge", "net/neoforged"]:
            stub_dir = mod_dest / stub_pkg
            if stub_dir.exists():
                shutil.rmtree(stub_dir)

        meta_inf = mod_dest / "META-INF"
        meta_inf.mkdir(parents=True, exist_ok=True)
        manifest_file = WORKSPACE / "build" / f"MANIFEST_{mod}.MF"
        manifest_content = (
            b"Manifest-Version: 1.0\r\n"
            b"MixinConfigs: hyperion.mixins.json\r\n"
            b"Implementation-Title: Hyperion Optimizer\r\n"
            b"Implementation-Version: 1.0.3\r\n"
            b"Specification-Title: hyperion_optimizer\r\n"
            b"Specification-Version: 1.0.3\r\n\r\n"
        )
        with open(manifest_file, "wb") as mf:
            mf.write(manifest_content)

        # Copy LICENSE into JAR root and META-INF for strict Open Source compliance
        shutil.copy2(WORKSPACE / "LICENSE", mod_dest / "LICENSE")
        shutil.copy2(WORKSPACE / "LICENSE", meta_inf / "LICENSE")

        jar_name = f"hyperion-optimizer-{mod}-1.0.3.jar"
        jar_path = LIBS_DIR / jar_name
        if jar_path.exists():
            jar_path.unlink()
        run_cmd(f'{JAR_CMD} -cfm "{jar_path.as_posix()}" "{manifest_file.as_posix()}" -C "{mod_dest.as_posix()}" .')
        size_kb = jar_path.stat().st_size / 1024
        print(f"  [OK] {jar_name} ({size_kb:.1f} KB)")

def deploy_to_launcher():
    print("[5/6] Deploying builds to ElyPrismLauncher...")
    instances = [
        ("hyperion-optimizer-fabric-26.2-1.0.3.jar", Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/instances/Fabulously Optimized/minecraft/mods")),
        ("hyperion-optimizer-fabric-1.21.11-1.0.3.jar", Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/instances/Fabulously Optimized(3)/minecraft/mods")),
        ("hyperion-optimizer-fabric-1.21.4-1.0.3.jar", Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/instances/Fabulously Optimized/minecraft/mods")),
        ("hyperion-optimizer-fabric-1.20.1-1.0.3.jar", Path("C:/Users/Admin/AppData/Roaming/ElyPrismLauncher/instances/Fabulously Optimized(3)/minecraft/mods"))
    ]
    for jar_file, mods_dir in instances:
        src = LIBS_DIR / jar_file
        if src.exists() and mods_dir.exists():
            dest = mods_dir / jar_file
            shutil.copy2(src, dest)
            print(f"  [OK] Copied {jar_file} to {dest}")

def run_tests():
    print("[6/6] Running Hyperion Test Suite...")
    test_src = WORKSPACE / "common" / "src" / "test" / "java" / "com" / "hyperion" / "optimizer" / "HyperionTestRunner.java"
    test_dest = WORKSPACE / "target" / "classes"
    test_dest.mkdir(parents=True, exist_ok=True)
    common_dest = CLASSES_DIR / "common"
    stubs_dest = CLASSES_DIR / "stubs"

    classpath = f"{common_dest.as_posix()}{PATH_SEP}{stubs_dest.as_posix()}{PATH_SEP}{EXTRA_CP_STR}" if EXTRA_CP_STR else f"{common_dest.as_posix()}{PATH_SEP}{stubs_dest.as_posix()}"

    run_cmd(f'{JAVAC_CMD} -cp "{classpath}" -d "{test_dest.as_posix()}" "{test_src.as_posix()}"')
    out = run_cmd(f'{JAVA_CMD} -Djava.awt.headless=true -cp "{test_dest.as_posix()}{PATH_SEP}{classpath}" com.hyperion.optimizer.HyperionTestRunner')
    lines = [l for l in out.strip().split("\n") if "SUMMARY" in l or "STATUS" in l or "[PASS]" in l]
    print("\n".join(lines[-10:]))

if __name__ == "__main__":
    clean()
    compile_common()
    compile_submodules()
    run_tests()
    package_jars()
    deploy_to_launcher()
    print("\n[SUCCESS] All multi-version modules rebuilt and deployed successfully.")
