const SOCKET_URL = 'wss://mockapi.kiwoom.com:10000/api/dostk/websocket';
const tokenEndpoint = '/api/auth';

class WebSocketClient {
    constructor(uri) {
        this.uri = uri;
        this.websocket = null;
        this.connected = false;
        this.currentResponse = {};
        this.recentRegisteredKeys = [];
    }

    // WebSocket 서버 연결
    async connect() {
        let ACCESS_TOKEN;
        try{
            const response = await fetch(tokenEndpoint, {});
            if (!response.ok) {
                throw new Error(`Failed to fetch token: ${response.status}`);
            }
            const data = await response.json();
            ACCESS_TOKEN = data.accessToken.replaceAll("Bearer ", "");
        }
        catch (error) {
            console.error('토큰 획득 중 오류 발생:', error);
            return Promise.reject(error); // 연결 시도 전에 실패를 반환
        }

        return new Promise((resolve, reject) => {
            this.websocket = new WebSocket(this.uri);

            // 서버 연결이 열리면 호출되는 이벤트
            this.websocket.onopen = () => {
                this.connected = true;
                console.log('서버와 연결되었습니다.');

                // 로그인 패킷
                const loginPacket = {
                    trnm: 'LOGIN',
                    token: ACCESS_TOKEN
                };

                console.log('실시간 시세 서버로 로그인 패킷을 전송합니다.');
                // 웹소켓 연결 시 로그인 정보 전달
                this.sendMessage(loginPacket);
                resolve();
            };

            // 에러 발생 시 호출되는 이벤트
            this.websocket.onerror = (error) => {
                console.error(`Connection error: ${error}`);
                reject(error);
            };

            // 서버 연결이 닫혔을 때 호출되는 이벤트
            this.websocket.onclose = () => {
                console.log('WebSocket 연결이 종료되었습니다.');
                this.connected = false;
            };

            // 서버로부터 메시지를 수신할 때 호출되는 이벤트
            this.websocket.onmessage = (event) => {
                const response = JSON.parse(event.data);

                // 메시지 유형이 LOGIN일 경우 로그인 시도 결과 체크
                if (response.trnm === 'LOGIN') {
                    if (response.return_code !== 0) {
                        console.error('로그인 실패:', response.return_msg);
                        this.disconnect();
                    } else {
                        console.log('로그인 성공하였습니다.');
                    }
                }
                // 메시지 유형이 PING일 경우 수신값 그대로 송신
                else if (response.trnm === 'PING') {
                    this.sendMessage(response);
                }

                if (response.trnm !== 'PING') {
                    console.log('실시간 시세 서버 응답 수신:', response);
                    if (response.trnm === 'SYSTEM' && response.code === 'R10001'){
                        this.connect();
                        this.register(this.recentRegisteredKeys);
                        console.log("소켓 재연결");
                    }
                    else{
                        this.currentResponse = response;
                    }
                }
            };
        });
    }

    // websocket 메시지 전송
    sendMessage(message) {
        if (this.connected && this.websocket) {
            this.websocket.send(JSON.stringify(message));
            console.log(`Message sent:`, message);
        } else {
            console.warn('WebSocket 연결이 없습니다.');
        }
    }

    // websocket 강제 종료
    disconnect() {
        if (this.websocket) {
            this.websocket.close();
            this.connected = false;
            console.log('Disconnected from WebSocket server');
        }
    }

    register(items) {
        this.recentRegisteredKeys = items;
        setTimeout(() => {
            this.sendMessage({
                'trnm': 'REG', // 서비스명
                'grp_no': '1', // 그룹번호
                'refresh': '1', // 기존등록유지여부
                'data': [{ // 실시간 등록 리스트
                    'item': items, // 실시간 등록 요소
                    'type': ['0B'], // 실시간 항목
                }]
            });
        }, 1000);
    }

    getRecentResponse(){
        return this.currentResponse;
    }
}

// 실시간 호출 함수
export async function registerToSocket(items) {
    const websocketClient = new WebSocketClient(SOCKET_URL);
    await websocketClient.connect();

    websocketClient.register(items);

    return websocketClient;
}