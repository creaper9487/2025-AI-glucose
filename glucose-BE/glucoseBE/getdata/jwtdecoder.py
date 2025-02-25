import jwt

def jwtdecode(token):
    decoded_payload = jwt.decode(token, key="django-insecure-exl(^*@9z48y&2-7j)o33pa7_m48k^ql#ik6#qtwajdoj%&4i=", algorithms=["HS256"])
    print(decoded_payload)
    username = decoded_payload.get('username')
    return username