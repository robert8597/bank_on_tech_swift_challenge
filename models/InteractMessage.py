import base64


class InteractMessage:
    def __init__(self, sender_reference=None, service_code=None, message_type=None,
                 requestor=None, responder=None, format=None, payload=None):
        self.sender_reference = sender_reference
        self.service_code = service_code
        self.message_type = message_type
        self.requestor = requestor
        self.responder = responder
        self.format = format
        self.payload = payload

    def set_sender_reference(self, sender_reference):
        self.sender_reference = sender_reference

    def set_service_code(self, service_code):
        self.service_code = service_code

    def set_message_type(self, message_type):
        self.message_type = message_type

    def set_requestor(self, requestor):
        self.requestor = requestor

    def set_responder(self, responder):
        self.responder = responder

    def set_format(self, format):
        self.format = format

    def set_payload(self, xml_payload):
        payload = base64.b64encode(xml_payload.encode("utf-8")).decode("utf-8")
        self.payload = payload

    def get_payload(self):
        return self.payload

    def get_sender_reference(self):
        return self.sender_reference

    def get_service_code(self):
        return self.service_code

    def get_message_type(self):
        return self.message_type

    def get_requestor(self):
        return self.requestor

    def get_responder(self):
        return self.responder

    def get_format(self):
        return self.format

    def to_dict(self):
        return {
            "sender_reference": self.sender_reference,
            "service_code": self.service_code,
            "message_type": self.message_type,
            "requestor": self.requestor,
            "responder": self.responder,
            "format": self.format,
            "payload": self.payload
        }

    def __str__(self):
        return (
            f"sender_reference: {self.sender_reference}\n"
            f"service_code: {self.service_code}\n"
            f"message_type: {self.message_type}\n"
            f"requestor: {self.requestor}\n"
            f"responder: {self.responder}\n"
            f"format: {self.format}\n"
            f"payload: {self.payload}\n"
        )