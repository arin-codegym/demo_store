# Copy this file to secrets.local.ps1 and fill local-only values.
# secrets.local.ps1 is ignored by Git.

# GitHub repository that receives the secrets, for example:
# $Repo = "your-github-user/demo_store"
$Repo = ""

# Local path to the EC2 private key used by GitHub Actions SSH deploy.
# $Ec2SshKeyPath = "C:\\path\\to\\store-key.pem"
$Ec2SshKeyPath = ""