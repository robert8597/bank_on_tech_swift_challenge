import base64
import datetime
import hashlib
import json
import random
from base64 import b64encode

import jwt
import requests

import logging

# logging.basicConfig(level=logging.DEBUG)

host = "https://sandbox.swift.com"

proxies = {
    'http': 'userproxy.intranet.db.com:8080',
    'https': 'userproxy.intranet.db.com:8080',
}


def generate_xswift_signature_for_post(url, data):
    if isinstance(data, dict):
        data_as_json = json.dumps(data).replace(" ", "")
    else:
        data_as_json = data.replace(" ", "")

    private_key = get_private_key()

    aud_dynamic = url.replace("https://", "")
    digest = sha256_base64(data_as_json)
    expiration_time = calculate_expiration_time()

    nr_payload = {
        "aud": aud_dynamic,
        "sub": "CN=desktop, O=sandbox, O=swift",
        "jti": generateJti(),
        "exp": expiration_time,
        "iat": expiration_time - 1900,
        "digest": digest,
        "nbf": expiration_time - 905
    }

    header = {
        "typ": 'JWT',
        "alg": 'RS256',
        "x5c": get_certificate(),
    }

    x_swift_signature = jwt.encode(nr_payload, private_key, headers=header, algorithm='RS256')

    print(x_swift_signature)

    return x_swift_signature, data_as_json


def create_jwt(consumer_key, consumer_secret):
    private_key = get_private_key()

    expiration_time = calculate_expiration_time()

    payload = {
        "iat": expiration_time - 1900,
        "nbf": expiration_time - 905,
        "exp": expiration_time,
        "jti": generateJti(),
        "iss": consumer_key,
        "aud": 'sandbox.swift.com/oauth2/v1/token',
        "sub": 'CN=demo-swift-sandbox-consumer, O=Demo, L=London, S=London, C=GB',
    }

    header = {
        "typ": 'JWT',
        "alg": 'RS256',
        "x5c": get_certificate(),
    }

    assertion = jwt.encode(payload, private_key, headers=header, algorithm='RS256')

    data = {
        "grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer",
        "assertion": assertion,
        "scope": "swift.alliancecloud.api"
    }

    print(data)

    authstr = 'Basic ' + b64encode((consumer_key + ':' + consumer_secret).encode('utf-8')).decode(
        'utf-8')

    headers = {
        'Authorization': authstr,
        'Content-Type': 'application/x-www-form-urlencoded',
    }

    token_response = requests.post(host + "/oauth2/v1/token", headers=headers, data=data)
    response_data = token_response.json()

    if token_response.status_code != 200:
        raise Exception(f"Failed to obtain access token: {response_data}\n"
                        f"\n##### Check your consumer key and secret #####")

    return response_data['access_token']


def generateJti():
    newJti = ""
    charset = "abcdefghijklmnopqrstuvwxyz0123456789"
    for i in range(0, 12):
        newJti += random.choice(charset)
    return newJti


def sha256_base64(data):
    ascii_string = data.encode('utf-8')
    b64urlString = base64.urlsafe_b64encode(ascii_string).decode()
    digest_preb64 = hashlib.sha256(b64urlString.encode()).digest()
    return base64.b64encode(digest_preb64).decode()


def calculate_expiration_time():
    return int(datetime.datetime.now().timestamp() + datetime.timedelta(minutes=15).total_seconds())


def get_private_key():
    private_key_from_swift = requests.get(host + "/sandbox-selfsigned-dummy-secret/privatekey2")
    return f"-----BEGIN PRIVATE KEY-----\n{private_key_from_swift.text}\n-----END PRIVATE KEY-----"


def get_certificate():
    certificate = requests.get(host + "/sandbox-selfsigned-dummy-secret/certificate2")
    return [certificate.text]

def get_pacs_008_xml():
    with open("pacs_008.xml", "r") as file:
        return file.read()

def log(task, response, file_name):
    print(f"{task}\n"
          f"Response\n"
          f"Code: {response.status_code}\n"
          f"JSON: {response.text}\n")

    if response.status_code == 200:
        with open(file_name, "w") as file:
            json.dump(response.json(), file, indent=4)
