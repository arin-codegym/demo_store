export async function safeParseJson(response: Response, url?: string) {
  const text = await response.text();
  try {
    if (text.length === 0) {
      return { message: 'Empty response body', url };
    }
    return JSON.parse(text);
  } catch (e) {
    return { message: `Failed to parse JSON: ${text}`, url };
  }
}
