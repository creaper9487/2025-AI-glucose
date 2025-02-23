#!/usr/bin/env python
"""Django's command-line utility for administrative tasks."""
import os
import sys
import socket
import time


def get_local_ip():
    try:
        # 使用 UDP socket 連接到一個外部伺服器，但不會真的發送資料
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
        s.close()
        return local_ip
    except Exception as e:
        return f"無法獲取 IP: {e}"


def main():
    """Run administrative tasks."""
    os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'glucoseBE.settings')
    try:
        from django.core.management import execute_from_command_line
    except ImportError as exc:
        raise ImportError(
            "Couldn't import Django. Are you sure it's installed and "
            "available on your PYTHONPATH environment variable? Did you "
            "forget to activate a virtual environment?"
        ) from exc
    print("本機區域網 IP 地址:", get_local_ip())
    execute_from_command_line(sys.argv)



if __name__ == '__main__':
    main()
