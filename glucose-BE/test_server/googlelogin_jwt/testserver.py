from flask import Flask, request, jsonify, render_template

app = Flask(__name__)


# 建立根路由，返回前端頁面
@app.route('/')
def index():
    return render_template('index.html')


@app.route('/oauth-callback')
def oauth_callback():
    code = request.args.get('code')
    if not code:
        return 'Authorization failed.', 400

    # Send the authorization code to the back-end for token exchange
    return render_template('oauth_callback.html', code=code)


if __name__ == '__main__':
    app.run(debug=True, port=5173)
