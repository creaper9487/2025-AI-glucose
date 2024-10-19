from flask import Flask, render_template, jsonify
import requests

app = Flask(__name__)

# 建立根路由，返回前端頁面
@app.route('/')
def index():
    return render_template('index.html')

# 啟動 Flask 應用程式，監聽 5173 埠
if __name__ == '__main__':
    app.run(port=5173)
