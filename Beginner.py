import requests

import Utils
from models.InteractMessage import InteractMessage
from Utils import create_jwt, generate_xswift_signature_for_post, log


# Task 0: Set up the host
host = "?"

access_token = 'Bearer ' + create_jwt()
SERVICE_CODE = "swift.finplus!pc"
MESSAGE_TYPE = "pacs.008.001.13"
REQUESTOR = "ou=xxx,o=deutdeff,o=swift"
RESPONDER = "ou=xxx,o=bktrus33,o=swift"
FORMAT = "MX"


# Task 1: Post an Interact message
def post_interact_message():

    # Task 1.1: Create the InteractMessage object and set its properties
    msg = InteractMessage()
    msg.set_sender_reference("BankOnTech")
    # ... set other properties

    # Task 1.2: Prepare the XML payload for your Interact message, add the correct values inside the pacs_008.xml file
    msg.set_payload(Utils.get_pacs_008_xml())

    # Task 1.3: Create the URL for posting the Interact message to the correct endpoint
    url = f"{host}/?"

    x_swift_signature, data_as_json = generate_xswift_signature_for_post(url, msg.to_dict())
    message_post_response = requests.post(url, data=data_as_json,
                                          headers={'Authorization': access_token,
                                                   'X-SWIFT-Signature': x_swift_signature})

    log("Task 1: Post an Interact message (post_interact_message)", message_post_response, "response_post_interact_message.json")

    return message_post_response, msg


# Task 2: Retrieve a list of distributions
def get_distributions():

    # Task 2.1: Create the URL for retrieving the list of distributions from the correct endpoint
    url = f"{host}/?"

    get_distributions_response = requests.get(url, headers={'Authorization': access_token})

    log("Task 2: Retrieve a list of distributions (get_distributions)", get_distributions_response, "distributions_list.json")

    return get_distributions_response


# Task 3: Retrieve a InterAct message by its distribution ID
def get_interact_message():

    # Task 3.1: Create the URL for retrieving a InterAct message by its distribution ID from the correct endpoint
    url = f"{host}/?"

    get_interact_message_response = requests.get(url, headers={'Authorization': access_token})

    log("Task 3: Retrieve a InterAct message by its distribution ID (get_interact_message)", get_interact_message_response, "interAct_message.json")

    return get_interact_message_response, url


# Task 4: Post an Acknowledgement for a distribution
def post_ack():

    # Task 4.1: Create the URL for posting an Acknowledgement for a distribution to the correct endpoint
    url = f"{host}/?"

    data = {}
    x_swift_signature, data_as_json = generate_xswift_signature_for_post(url, data)

    post_ack_response = requests.post(url, data=data_as_json,
                                      headers={'Authorization': access_token,
                                               'X-SWIFT-Signature': x_swift_signature})

    log("Task 4: Post an Acknowledgement for a distribution (post_ack)", post_ack_response, "response_post_ack.json")

    return post_ack_response, url


if __name__ == '__main__':
    post_interact_message()
    get_distributions()
    get_interact_message()
    post_ack()
