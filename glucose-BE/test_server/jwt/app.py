from flask import Flask, request, jsonify, render_template

app = Flask(__name__)


# 建立根路由，返回前端頁面
@app.route('/')
def index():
    return render_template('index.html')

if __name__ == '__main__':
    app.run(debug=True, port=5173)
