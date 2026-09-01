"""
⚡ Hyperion Optimizer — Modrinth Release Manifest & Verification Generator
Generates per-file release metadata, hashes (SHA1/SHA512), loaders, and game_version mappings
compliant with Modrinth V2 Version API (POST /v2/version).
"""
import hashlib
import json
from pathlib import Path

WORKSPACE = Path(__file__).resolve().parent
LIBS_DIR = WORKSPACE / "build" / "libs"
OUTPUT_JSON = WORKSPACE / "build" / "modrinth_releases.json"

VERSION_MAP = {
    "fabric-1.16.5": (["fabric"], ["1.16.5"]),
    "forge-1.16.5": (["forge"], ["1.16.5"]),
    "fabric-1.17.1": (["fabric"], ["1.17", "1.17.1"]),
    "forge-1.17.1": (["forge"], ["1.17", "1.17.1"]),
    "fabric-1.18.2": (["fabric"], ["1.18", "1.18.1", "1.18.2"]),
    "forge-1.18.2": (["forge"], ["1.18.2"]),
    "fabric-1.19.2": (["fabric"], ["1.19", "1.19.1", "1.19.2"]),
    "forge-1.19.2": (["forge"], ["1.19.2"]),
    "fabric-1.19.4": (["fabric"], ["1.19.3", "1.19.4"]),
    "forge-1.19.4": (["forge"], ["1.19.4"]),
    "fabric-1.20.1": (["fabric"], ["1.20", "1.20.1"]),
    "forge-1.20.1": (["forge"], ["1.20", "1.20.1"]),
    "fabric-1.20.4": (["fabric"], ["1.20.2", "1.20.3", "1.20.4"]),
    "neoforge-1.20.4": (["neoforge"], ["1.20.4"]),
    "fabric-1.20.6": (["fabric"], ["1.20.5", "1.20.6"]),
    "neoforge-1.20.6": (["neoforge"], ["1.20.6"]),
    "fabric-1.21.1": (["fabric"], ["1.21", "1.21.1"]),
    "neoforge-1.21.1": (["neoforge"], ["1.21.1"]),
    "fabric-1.21.4": (["fabric"], ["1.21.2", "1.21.3", "1.21.4"]),
    "neoforge-1.21.4": (["neoforge"], ["1.21.4"]),
    "fabric-1.21.11": (["fabric"], ["1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11"]),
    "neoforge-1.21.11": (["neoforge"], ["1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11"]),
    "fabric-26.1": (["fabric"], ["26.1"]),
    "neoforge-26.1": (["neoforge"], ["26.1"]),
    "fabric-26.2": (["fabric"], ["26.2"]),
    "neoforge-26.2": (["neoforge"], ["26.2"]),
}

def get_hashes(file_path: Path):
    data = file_path.read_bytes()
    return {
        "sha1": hashlib.sha1(data).hexdigest(),
        "sha512": hashlib.sha512(data).hexdigest(),
        "size": len(data)
    }

def generate_manifest():
    releases = []
    for mod_key, (loaders, game_versions) in VERSION_MAP.items():
        jar_name = f"hyperion-optimizer-{mod_key}-1.0.3.jar"
        jar_path = LIBS_DIR / jar_name
        if not jar_path.exists():
            continue
        h = get_hashes(jar_path)
        loader_tag = loaders[0]
        release_entry = {
            "name": f"Hyperion Optimizer 1.0.3 for {loader_tag.capitalize()} {game_versions[-1]}",
            "version_number": f"1.0.3+{mod_key}",
            "game_versions": game_versions,
            "version_type": "release",
            "loaders": loaders,
            "featured": True,
            "status": "listed",
            "requested_status": "listed",
            "project_id": "hyperion-optimizer",
            "file_name": jar_name,
            "hashes": {
                "sha1": h["sha1"],
                "sha512": h["sha512"]
            },
            "file_size": h["size"],
            "primary": True
        }
        releases.append(release_entry)

    OUTPUT_JSON.parent.mkdir(parents=True, exist_ok=True)
    with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
        json.dump(releases, f, indent=2)

    print(f"Generated Modrinth release manifest for {len(releases)} packages: {OUTPUT_JSON}")

if __name__ == "__main__":
    generate_manifest()
