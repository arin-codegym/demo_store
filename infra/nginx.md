# Nginx Reverse Proxy

Production traffic is terminated by Nginx on the EC2 host.

## Responsibilities

- Serve HTTPS on ports 80 and 443.
- Proxy frontend traffic to the Next.js container on `127.0.0.1:3000`.
- Proxy backend API traffic to the Spring Boot container on `127.0.0.1:8080`.
- Keep public users away from direct container ports. Only Nginx should be public.

## Example Server Block

```nginx
server {
    listen 80;
    server_name example.com www.example.com;

    location /api/backend/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## HTTPS

Use Certbot after DNS points to the EC2 public IP:

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d example.com -d www.example.com
```

## Security Group

The EC2 security group should expose only:

- `80` from `0.0.0.0/0`
- `443` from `0.0.0.0/0`
- `22` from a trusted IP range, when possible

Container ports `3000` and `8080` can still be bound locally, but they should not be publicly open in the EC2 security group.