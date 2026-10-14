param()

$salt = "KTOP_HAMMERHEAD_SECRET_SALT_2026_@!"
$payloads = @("7777", "9999", "A1B2", "DEV1", "BETA")

$sha = [System.Security.Cryptography.SHA256]::Create()

foreach ($p in $payloads) {
    $str = "$salt`:$p"
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($str)
    $hash = $sha.ComputeHash($bytes)
    $hex = ""
    foreach ($b in $hash) {
        $hex += $b.ToString("X2")
    }
    $checksum = $hex.Substring(0, 8)
    $clean = "KTOP" + $p + $checksum
    $formatted = "$($clean.Substring(0,4))-$($clean.Substring(4,4))-$($clean.Substring(8,4))-$($clean.Substring(12,4))"
    Write-Host "KEY: $formatted"
}
