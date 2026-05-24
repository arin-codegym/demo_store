
# Docker use

A brief description of what this project does and who it's for


## Installation




## Usage/Examples

- Xem tất cả image đang chay

```bash
  docker compose ps --format "table {{.Name}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"
```
- xem log fontend

```bash
  docker compose logs -f
  docker compose logs -f frontend
  docker compose logs -f backend
```
- Sửa code nhưng không chạy rebuild image

```bash
  docker compose restart frontend
  docker compose restart backend
```
- Stop theo thứ tự tưng image -> xóa -> đọc laạ file mới

```bash
    docker compose stop redis frontend backend
    docker rm -f ubuntu_redis_1 ubuntu_frontend_1 ubuntu_backend_1 2>/dev/null || true
    docker compose up -d redis
    docker compose up -d frontend backend
```
- tắt và xóa container, network của compose

```bash
    docker compose down
```
- nôi dung

```bash
  npm run test
```
- nôi dung

```bash
  npm run test
```
- nôi dung

```bash
  npm run test
```

## Thao tác file ubuntu

- Ví dụ xóa file store-app

```bash
  mkdir -p ~/store-app
```
- Sửa file

```bash
  nano docker-compose.yml
```
- Ví dụ xóa file store-app

```bash
  mkdir -p ~/store-app
```


## Appendix

Any additional information goes here


# Hi, I'm Katherine! 👋


## Other Common Github Profile Sections
👩‍💻 I'm currently working on...

🧠 I'm currently learning...

👯‍♀️ I'm looking to collaborate on...

🤔 I'm looking for help with...

💬 Ask me about...

📫 How to reach me...

😄 Pronouns...

⚡️ Fun fact...


## Optimizations

What optimizations did you make in your code? E.g. refactors, performance improvements, accessibility


## Run Locally

Clone the project

```bash
  git clone https://link-to-project
```

Go to the project directory

```bash
  cd my-project
```

Install dependencies

```bash
  npm install
```

Start the server

```bash
  npm run start
```


## Tech Stack

**Client:** React, Redux, TailwindCSS

**Server:** Node, Express


## Used By

This project is used by the following companies:

- Company 1
- Company 2


## FAQ

#### Question 1

Answer 1

#### Question 2

Answer 2


## API Reference

#### Get all items

```http
  GET /api/items
```

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `api_key` | `string` | **Required**. Your API key |

#### Get item

```http
  GET /api/items/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `string` | **Required**. Id of item to fetch |

#### add(num1, num2)

Takes two numbers and returns the sum.


## Demo

Insert gif or link to demo


## Environment Variables

To run this project, you will need to add the following environment variables to your .env file

`API_KEY`

`ANOTHER_API_KEY`


## Lessons Learned

What did you learn while building this project? What challenges did you face and how did you overcome them?


## Screenshots

![App Screenshot](https://dummyimage.com/468x300?text=App+Screenshot+Here)

