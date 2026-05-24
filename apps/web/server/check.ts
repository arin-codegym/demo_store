const list = [
  'refreshToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwNGJiZGE2YS0yMWFkLTQ4NDQtOWU2OS1mM2VkMDdhODA2MWIiLCJyb2xlcyI6IiIsImlzcyI6InF1b2NodXktc3RvcmUiLCJleHAiOjE3NzE2MTYzMTAsInVzZXJOYW1lIjoiQXJpbiIsImlhdCI6MTc3MTAxMTUxMCwiZW1haWwiOiJjYW9xdW9jaHV5MjFAZ21haWwuY29tIn0.twz5zim2rT6OYsmKcnLt_G2QBU1ORaPEQc-28_TXQpk; Path=/; Max-Age=604800; Expires=Fri, 20 Feb 2026 19:38:30 GMT; HttpOnly; SameSite=Lax',
  'accessToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwNGJiZGE2YS0yMWFkLTQ4NDQtOWU2OS1mM2VkMDdhODA2MWIiLCJyb2xlcyI6IlJPTEVfQURNSU4iLCJpc3MiOiJxdW9jaHV5LXN0b3JlIiwiZXhwIjoxNzcxMDE1MTEwLCJ1c2VyTmFtZSI6IkFyaW4iLCJpYXQiOjE3NzEwMTE1MTAsImVtYWlsIjoiY2FvcXVvY2h1eTIxQGdtYWlsLmNvbSJ9.0y4oqQHScH2QMIRU9y1MhBP8JvioNHWHNPBh4G1DmOU; Path=/; Max-Age=3600; Expires=Fri, 13 Feb 2026 20:38:30 GMT; HttpOnly; SameSite=Lax',
];
const refreshToken = list.find((cookie) => cookie.includes('refreshToken='));
const accessToken = list.find((cookie) => cookie.includes('accessToken='));
if (refreshToken && accessToken) {
  console.log(refreshToken.split(';'));
}
