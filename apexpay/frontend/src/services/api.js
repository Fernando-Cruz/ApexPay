import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('apexpay_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const checkAccountExists = async (accountUuid) => {
  const response = await api.get(`/accounts/${accountUuid}`);
  return response.data;
};

export const loginUser = async (email, password) => {
  const response = await api.post('/auth/login', {
    email: email.trim(),
    password: password
  });

  const token =
    response.data?.token ||
    response.data?.accessToken ||
    response.data?.jwt;
  const account =
    response.data?.accountNumber ||
    response.data?.account ||
    response.data?.user?.accountNumber;

  if (token) {
    localStorage.setItem('apexpay_token', token);
    localStorage.setItem('apexpay_user', email);
    if (account) {
      localStorage.setItem('apexpay_account', account);
    }
  }
  return response.data;
};

export const executeTransfer = async (destinationAccount, amount, idempotencyKey) => {
  const response = await api.post(
    '/transactions/transfer', // Confirme o endpoint correto da sua API
    {
      // Certifique-se de que as chaves correspondam ao DTO do seu Spring Boot:
      destinationAccount: destinationAccount.trim(), // Ou 'targetAccount', 'destinationAccountId', etc.
      amount: Number(amount)              // Certifique-se de converter para número
    },
    {
      headers: {
        'Idempotency-Key': idempotencyKey
      }
    }
  );

  return {
    keyUsed: idempotencyKey,
    data: response.data
  };
};


export default api;