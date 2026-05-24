$ErrorActionPreference = "Stop"

# Root is the frontend repo folder. This makes the script work no matter
# whether you run it from the repo root or from another current directory.
$Root = Resolve-Path (Join-Path $PSScriptRoot "..")

# Local-only config files. They are intentionally ignored by Git because they
# contain repo name, EC2 key path, Docker Hub token, and production URLs.
$ConfigFile = Join-Path $Root "deploy\secrets.local.ps1"
$GhSecretsFile = Join-Path $Root "deploy\github-actions-secrets.env"

if (-not (Test-Path $ConfigFile)) { throw "Missing $ConfigFile" }

# Dot-source the config file so variables inside it become available here:
# $Repo tells GitHub CLI which GitHub repo receives the secrets.
# $Ec2SshKeyPath tells the script where the local EC2 .pem private key is.
. $ConfigFile

if (-not $Repo) { throw "Missing `$Repo in deploy\secrets.local.ps1" }
if (-not $Ec2SshKeyPath) { throw "Missing `$Ec2SshKeyPath in deploy\secrets.local.ps1" }
if (-not (Test-Path $GhSecretsFile)) { throw "Missing $GhSecretsFile" }
if (-not (Test-Path $Ec2SshKeyPath)) { throw "Missing EC2 SSH key: $Ec2SshKeyPath" }

# Verifies that `gh auth login` has already been completed on this machine.
gh auth status

Write-Host "Sync normal GitHub Actions secrets..."
# Read deploy/github-actions-secrets.env manually so the file can contain
# helpful comments and blank lines. Each non-comment KEY=value line becomes one
# GitHub Actions secret in the repo selected by -R, for example:
# gh secret set EC2_HOST -R arin-codegym/store
foreach ($rawLine in Get-Content $GhSecretsFile) {
    $line = $rawLine.Trim()
    if (-not $line -or $line.StartsWith("#")) {
        continue
    }

    $separator = $line.IndexOf("=")
    if ($separator -lt 1) {
        throw "Invalid secret line in ${GhSecretsFile}: $rawLine"
    }

    $name = $line.Substring(0, $separator).Trim()
    $value = $line.Substring($separator + 1)

    Write-Host "Sync $name..."
    $value | gh secret set $name -R $Repo
}

Write-Host "Sync EC2_SSH_KEY..."
# EC2_SSH_KEY is multi-line, so it is read from the .pem file and piped to gh.
# GitHub Actions uses this secret to SSH into EC2.
Get-Content -Raw $Ec2SshKeyPath | gh secret set EC2_SSH_KEY -R $Repo

Write-Host "Current GitHub secrets:"
# This lists only secret names, not secret values.
gh secret list -R $Repo