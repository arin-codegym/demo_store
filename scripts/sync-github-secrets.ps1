$ErrorActionPreference = "Stop"

# Root is the monorepo folder. This script syncs all GitHub Actions secrets
# needed by both apps/web and apps/api into one GitHub repository.
$Root = Resolve-Path (Join-Path $PSScriptRoot "..")

$ConfigFile = Join-Path $Root "deploy\secrets.local.ps1"
$GhSecretsFile = Join-Path $Root "deploy\github-actions-secrets.env"
$ProdEnvFile = Join-Path $Root "deploy\prod.env"
$InfraEnvFile = Join-Path $Root "deploy\infra.env"

if (-not (Test-Path $ConfigFile)) { throw "Missing $ConfigFile. Copy deploy\secrets.local.example.ps1 first." }
if (-not (Test-Path $GhSecretsFile)) { throw "Missing $GhSecretsFile. Copy deploy\github-actions-secrets.example.env first." }
if (-not (Test-Path $ProdEnvFile)) { throw "Missing $ProdEnvFile. Copy deploy\prod.example.env first." }

. $ConfigFile

if (-not $Repo) { throw "Missing `$Repo in deploy\secrets.local.ps1" }
if (-not $Ec2SshKeyPath) { throw "Missing `$Ec2SshKeyPath in deploy\secrets.local.ps1" }
if (-not (Test-Path $Ec2SshKeyPath)) { throw "Missing EC2 SSH key: $Ec2SshKeyPath" }

gh auth status

Write-Host "Sync KEY=value GitHub Actions secrets..."
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
Get-Content -Raw $Ec2SshKeyPath | gh secret set EC2_SSH_KEY -R $Repo

Write-Host "Sync PROD_ENV_FILE..."
Get-Content -Raw $ProdEnvFile | gh secret set PROD_ENV_FILE -R $Repo

if (Test-Path $InfraEnvFile) {
    Write-Host "Sync INFRA_ENV_FILE..."
    Get-Content -Raw $InfraEnvFile | gh secret set INFRA_ENV_FILE -R $Repo
} else {
    Write-Host "Skip INFRA_ENV_FILE: $InfraEnvFile not found."
}

Write-Host "Current GitHub secrets:"
gh secret list -R $Repo
