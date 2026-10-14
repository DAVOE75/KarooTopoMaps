import hashlib
import sys

SALT = "KTOP_HAMMERHEAD_SECRET_SALT_2026_@!"

def compute_checksum(payload: str) -> str:
    inp = f"{SALT}:{payload}".encode("utf-8")
    h = hashlib.sha256(inp).hexdigest().upper()
    return h[:8]

def generate_key(seed: str) -> str:
    md5 = hashlib.md5(seed.encode("utf-8")).hexdigest().upper()
    payload = md5[:4]
    checksum = compute_checksum(payload)
    clean = f"KTOP{payload}{checksum}"
    return f"{clean[0:4]}-{clean[4:8]}-{clean[8:12]}-{clean[12:16]}"

def verify_key(key: str) -> bool:
    clean = key.strip().upper().replace("-", "").replace(" ", "")
    if len(clean) != 16 or not clean.startswith("KTOP"):
        return False
    payload = clean[4:8]
    checksum = clean[8:16]
    return checksum == compute_checksum(payload)

if __name__ == "__main__":
    if len(sys.argv) > 1:
        seed = sys.argv[1]
    else:
        seed = "david_test"
    key = generate_key(seed)
    print(f"Seed: {seed}")
    print(f"Generated Key: {key}")
    print(f"Validates: {verify_key(key)}")
