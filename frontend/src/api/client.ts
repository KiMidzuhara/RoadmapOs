import axios from 'axios';

export const api = axios.create({
    baseURL: 'http://localhost:8080/api/v1', // Твой Java Backend
    headers: {
        'Content-Type': 'application/json',
    },
});

// Перехватчик ошибок (чтобы видеть их в консоли)
api.interceptors.response.use(
    (response) => response,
    (error) => {
        const message = error.response?.data?.message || "Unknown error";
        console.error("API Error:", message);
        // Тут можно добавить всплывающее уведомление (Toast)
        return Promise.reject(error);
    }
);