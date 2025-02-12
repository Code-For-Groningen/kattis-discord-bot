import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function stringToNumber(s: string, max: number): number {
  // string is any string of chararacters (a name of a person)
  // and it has to be converted to a number
  // that is within the range of 0 to max
  // and it has to be a whole number

  // the number is calculated by summing the char codes
  // of all the characters in the string
  // and then taking the remainder of dividing that sum by max
  // and returning the result
  let sum = 0;
  for (let i = 0; i < s.length; i++) {
    sum += s.charCodeAt(i);
  }
  return sum % max;
}
