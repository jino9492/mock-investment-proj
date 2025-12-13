// ⭐ 서버의 WebSocket 브로드캐스팅 엔드포인트로 변경
// 이 URL은 서버에 구현된 RealtimeWebSocket.java의 @ServerEndpoint 경로와 일치해야 합니다.
const getSocketUrl = () => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    return protocol + '//' + host + '/ws/realtime';
};

class WebSocketClient {
    constructor(uri) {
        this.uri = uri;
        this.websocket = null;
        this.connected = false;
        this.dataUpdateCallback = null;
    }

    registerDataUpdateCallback(callback) {
        this.dataUpdateCallback = callback;
    }

    async connect() {
        return new Promise((resolve, reject) => {
            this.websocket = new WebSocket(this.uri);

            this.websocket.onopen = () => {
                this.connected = true;
                console.log('서버 브로드캐스팅 소켓과 연결되었습니다.');
                resolve();
            };

            this.websocket.onerror = (error) => {
                console.error(`Connection error:`, error);
                this.connected = false;
                reject(error);
            };

            this.websocket.onclose = () => {
                console.log('서버 WebSocket 연결이 종료되었습니다.');
                this.connected = false;
            };

            this.websocket.onmessage = (event) => {
                try {
                    const response = JSON.parse(event.data);

                    if(this.dataUpdateCallback) {
                        this.dataUpdateCallback(response);
                    }

                    console.log(response)
                } catch (e) {
                    console.error('WebSocket 응답 파싱 오류:', e);
                }
            };
        });
    }

    sendMessage(message) {
        if (this.connected && this.websocket) {
            this.websocket.send(JSON.stringify(message));
            console.log(`Message sent:`, message);
        } else {
            console.warn('WebSocket 연결이 없습니다.');
        }
    }

    disconnect() {
        if (this.websocket) {
            this.websocket.close();
            this.connected = false;
            console.log('Disconnected from WebSocket server');
        }
    }
}

export async function registerToSocket() {
    const websocketClient = new WebSocketClient(getSocketUrl());

    try {
        await websocketClient.connect();
    } catch (error) {
        console.error("WebSocket 연결 시도 실패:", error);
    }


    return websocketClient;
}