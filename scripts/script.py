from bs4 import BeautifulSoup
import requests
from concurrent.futures import ThreadPoolExecutor
import json

result = {}

# Function to process each URL
def fetch_data(url):
    try:
        response = requests.get(url)
        soup = BeautifulSoup(response.text, 'html.parser')

        title = soup.select('.image_info-text-main-header')[0].text

        # Thorough logging to see what's going on
        print(f"Title: {title}")
        return title, url
    except Exception as e:
        print(f"Error: {e} for URL: {url}")
        return None, None

# Main function to read URLs and run them in parallel
def main():
    with open('links.txt', 'r') as f:
        urls = [line.strip() for line in f.readlines()]

    # Using ThreadPoolExecutor to parallelize the requests
    with ThreadPoolExecutor(max_workers=50) as executor:
        futures = {executor.submit(fetch_data, url): url for url in urls}

        for future in futures:
            try:
                title, url = future.result()
                if title and url:
                    result[url] = title
                    print(f"Count: {len(result)} / {len(urls)}")
            except Exception as e:
                print(f"Error processing future: {e}")

    print(result)

    # Dump to json in result.txt
    with open('result.txt', 'w') as f:
        f.write(json.dumps(result))

if __name__ == "__main__":
    main()