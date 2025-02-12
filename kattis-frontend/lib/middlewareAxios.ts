import axios, { AxiosError } from "axios";

/**
 * Middleware for Axios. It adds a redirect to /login whenever we receive a 401
 * from the backend.
 */
const middlewareAxios = () => {
  const instance = axios.create();

  // Add a response interceptor
  instance.interceptors.response.use(
    function (response) {
      return response;
    },
    function (error: AxiosError) {
      // Do something with response error
      if (error?.response?.status === 401) {
        window.location.href = "/login";
      }

      return Promise.reject(error);
    },
  );
  return instance;
};

export default middlewareAxios;
