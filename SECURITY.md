# Security Policy

## Scope

This is an educational project. Do not use it to process real customer data or production credentials.

## Reporting a vulnerability

Do not publish credentials, exploit details, personal data, or a live vulnerability in a public issue.

Use GitHub's private vulnerability reporting feature if it is enabled for this repository. If it is unavailable, contact the repository owner through the GitHub profile and provide only the minimum information needed to coordinate a private report.

## Secrets and sensitive data

- Store credentials in environment variables or a secret manager.
- Never commit real tokens, passwords, private keys, database dumps, or personal data.
- Treat any credential committed to Git as compromised, even if it was later deleted from the current branch.
- Rotate exposed credentials at the issuing system before considering history cleanup.
- Coordinate history rewrites with contributors because existing clones and branches may need to be recreated.
