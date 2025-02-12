import { AxiosPromise, AxiosRequestConfig } from "axios";
import middlewareAxios from "./middlewareAxios";
import * as _ from "lodash";

const axios = middlewareAxios();

const getBearerTokenConfig = () => {
  const token = localStorage.getItem("access");
  if (token) {
    return { headers: { Authorization: `Bearer ${token}` } };
  }
  return {};
};

/**
 * Runs a get call on a url
 * @param url the url to get the data from
 * @param config any configuration to pass to axios
 */
export function fetchWithAuth<Type>(
  url: string,
  config?: AxiosRequestConfig,
): AxiosPromise<Type> {
  return axios.get<Type>(url, _.merge(getBearerTokenConfig(), config));
}

/**
 * Runs a post call on a url
 * @param url the url to post the data to
 * @param data the data to send to the backend
 * @param config any configuration to pass to axios
 */
export function postWithAuth<Type>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): AxiosPromise<Type> {
  return axios.post<Type>(url, data, _.merge(getBearerTokenConfig(), config));
}

/**
 * Runs a put call on a url
 * @param url the url to post the data to
 * @param data the data to send to the backend
 * @param config any configuration to pass to axios
 */
export const putWithAuth = (
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): AxiosPromise => {
  return axios.put(url, data, _.merge(getBearerTokenConfig(), config));
};

/**
 * Runs a patch call on a url
 * @param url the url to call the patch on (usually includes an id parameter)
 * @param data the data to send to the backend
 * @param config any configuration to pass to axios
 */
export const patchWithAuth = (
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): AxiosPromise => {
  return axios.patch(url, data, _.merge(getBearerTokenConfig(), config));
};

/**
 * Runs a delete call on a url
 * @param url the url to call the delete on (usually includes an id parameter)
 * @param config any configuration to pass to axios
 */
export const deleteWithAuth = (
  url: string,
  config?: AxiosRequestConfig,
): AxiosPromise => {
  return axios.delete(url, _.merge(getBearerTokenConfig(), config));
};
