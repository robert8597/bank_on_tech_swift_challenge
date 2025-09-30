import requests

from Utils import create_jwt, generate_xswift_signature_for_post

host = "https://sandbox.swift.com"
access_token = 'Bearer ' + create_jwt("5msX6Fd3lY6UeUy0xe8A8AW3TtbNAwMI", "sKTyGChnlMOFjntk")


def get_distributions():
    get_distributions_response = requests.get(host + "/alliancecloud/v2/distributions",
                                              headers={'Authorization': access_token})




    print("get_distributions\n", get_distributions_response.json(), "\n")


def post_fin_message():
    data = {
        "sender_reference": "LOW-000082027617",
        "message_type": "fin.101",
        "sender": "SWLLUS33XXXX",
        "receiver": "SWHQBEBBXXXX",
        "payload": "DQo6MjA6TE9XLTAwMDA4MjAyNzYxNw0KOjI4RDoxMjM0NS8xMjM0NQ0KOjUwTDp4DQo6NTBIOi94DQp4DQo6MzA6OTYxMTIzDQo6MjE6TE9XLTAwMDA4MjAyNzYxNw0KOjMyQjpVU0QxLDM0DQo6NTJBOkNIQVNVUzMzDQo6NTk6eA0KOjcxQTpTSEENCjoyMTpMT1ctMDAwMDgyMDI3NjE3DQo6MzJCOlVTRDEsMzQNCjo1MkE6Q0hBU1VTMzMNCjo1OTp4DQo6NzFBOlNIQQ=="
    }

    url = host + "/alliancecloud/v2/fin/messages"

    x_swift_signature, data_as_json = generate_xswift_signature_for_post(url, data)

    message_post_response = requests.post(url, data=data_as_json,
                                          headers={'Authorization': access_token,
                                                   'X-SWIFT-Signature': x_swift_signature})

    print("post_fin_message\n", message_post_response.json(), "\n")


def post_interact_message():
    data = {
        "sender_reference": "TSORCASWHQCHZ0XXX_CAN_setr",
        "service_code": "swift.if.ia",
        "message_type": "setr.017.001.04",
        "requestor": "ou=orca,o=swllus33,o=swift",
        "responder": "ou=mfunds,o=swhqbebb,o=swift",
        "format": "MX",
        "payload": "PGVudmVsb3BlOkVudmVsb3BlIHhtbG5zOmVudmVsb3BlPSJ1cm46c3dpZnQ6eHNkOmVudmVsb3BlIj48QXBwSGRyIHhtbG5zPSJ1cm46c3dpZnQ6eHNkOiRhaFYxMCI+PE1zZ1JlZj5JRTE5MDgwODUwOTUyNjA3PC9Nc2dSZWY+PENyRGF0ZT4yMDE5LTA4LTA4VDEzOjE4OjQ4PC9DckRhdGU+PC9BcHBIZHI+PERvY3VtZW50IHhtbG5zPSJ1cm46aXNvOnN0ZDppc286MjAwMjI6dGVjaDp4c2Q6c2V0ci4wMTcuMDAxLjA0Ij48T3JkckN4bFN0c1JwdD48TXNnSWQ+PElkPklFMTkwODA4NTA5NTI2MDc8L0lkPjxDcmVEdFRtPjIwMTktMDgtMDhUMTM6MTg6NDg8L0NyZUR0VG0+PC9Nc2dJZD48U3RzUnB0PjxJbmR2Q3hsU3RzUnB0PjxPcmRyUmVmPkdaVUJMMDQwMDQ1NTQ2OEI8L09yZHJSZWY+PEN4bFN0cz48U3RzPkNBTkQ8L1N0cz48L0N4bFN0cz48L0luZHZDeGxTdHNScHQ+PC9TdHNScHQ+PC9PcmRyQ3hsU3RzUnB0PjwvRG9jdW1lbnQ+PC9lbnZlbG9wZTpFbnZlbG9wZT4="
    }

    url = host + "/alliancecloud/v2/interact/messages"

    x_swift_signature, data_as_json = generate_xswift_signature_for_post(url, data)

    message_post_response = requests.post(url, data=data_as_json,
                                          headers={'Authorization': access_token,
                                                   'X-SWIFT-Signature': x_swift_signature})

    print("post_interact_message\n", message_post_response.json(), "\n")


def get_fin_message(distribution_id):
    get_fin_message_response = requests.get(host + "/alliancecloud/v2/fin/messages/" + distribution_id,
                                            headers={'Authorization': access_token})
    print(f"get_fin_message {distribution_id}\n", get_fin_message_response.json(), "\n")


def get_interact_message(distribution_id):
    get_interact_message_response = requests.get(host + "/alliancecloud/v2/interact/messages/" + distribution_id,
                                                 headers={'Authorization': access_token})
    print(f"get_interact_message {distribution_id}\n", get_interact_message_response.json(), "\n")


def get_fin_transmission_report(distribution_id):
    get_fin_transmission_report_response = requests.get(
        host + "/alliancecloud/v2/fin/transmission-reports/" + distribution_id,
        headers={'Authorization': access_token})
    print(f"get_fin_transmission_report {distribution_id}\n", get_fin_transmission_report_response.json(), "\n")


def get_interact_transmission_report(distribution_id):
    get_interact_transmission_report_response = requests.get(
        host + "/alliancecloud/v2/interact/transmission-reports/" + distribution_id,
        headers={'Authorization': access_token})
    print(f"get_interact_transmission_report {distribution_id}\n", get_interact_transmission_report_response.json(),
          "\n")


def post_ack(distribution_id):
    url = f"{host}/alliancecloud/v2/distributions/{distribution_id}/acks"
    data = {}

    x_swift_signature, data_as_json = generate_xswift_signature_for_post(url, data)

    post_ack_response = requests.post(url, data=data_as_json,
                                      headers={'Authorization': access_token,
                                               'X-SWIFT-Signature': x_swift_signature})

    print(f"post_ack {distribution_id}\n", post_ack_response.status_code, "\n")


if __name__ == '__main__':
    # get_distributions()
    post_fin_message()
    # post_interact_message()
    # get_fin_message("44984189500")
    # get_interact_message("44984189502")
    # get_fin_transmission_report("44984189498")
    # get_interact_transmission_report("44984189498")
    # post_ack("44984189498")
