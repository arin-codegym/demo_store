### NGINX - Setup

1) Cài Nginx
```sh
    sudo apt update
    sudo apt install -y nginx
```



2) Tạo config reverse proxy
```
sudo tee /etc/nginx/sites-available/app > /dev/null <<'EOF'
server {
    listen 80;
    server_name 15.135.235.185;

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

```
- Mở file hoăc edit
```sh
sudo nano /etc/nginx/sites-available/app
```
- Có thể viết lại an toàn hơn
  ```
  server {
    listen 80;
    server_name 3.106.154.232;

    location /api/backend/ {
        proxy_pass http://127.0.0.1:8080/;
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

- Kiểm tra file đã có thật chưa
```
ls -l /etc/nginx/sites-available/app
cat /etc/nginx/sites-available/app 
```

  




3) Tạo config reverse proxy
```
sudo rm -f /etc/nginx/sites-enabled/default
sudo ln -sf /etc/nginx/sites-available/app /etc/nginx/sites-enabled/app
```




4) Kiểm tra config và restart Nginx

    ```
    sudo nginx -t
    sudo systemctl restart nginx
    ```
- Nếu chỉ sửa file thi 
```
	sudo systemctl reload nginx
	sudo systemctl status nginx --no-pager
	sudo systemctl enable nginx
```
- Kiểm tra link đang trỏ đúng chưa
```
ls -l /etc/nginx/sites-enabled/
```
- Nếu muốn kiểm tra cực nhanh file nào đang thiếu, chạy:
    ```
    ls -l /etc/nginx/sites-available/
    ls -l /etc/nginx/sites-enabled/
    ```

5) Kiểm tra frontend/backend có đang chạy local không
```
curl http://127.0.0.1:3000
curl http://127.0.0.1:8080/api/product/featuredProducts
hoặc
curl http://127.0.0.1:8080/health
```
- Kiêm tra ngoài
```
curl http://15.135.235.185
curl http://3.106.154.232/
curl http://15.135.235.185/api/product/featuredProducts
```




