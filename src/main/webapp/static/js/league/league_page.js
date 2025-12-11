/**
 * 리그 페이지 메인 클래스
 * API 연동 및 데이터 렌더링 관리
 */
import {getPriceColorClass, formatPriceChange} from "../common/price_color_utils.js";

class LeaguePage {
    constructor() {
        this.updateIntervalId = null;
        this.UPDATE_INTERVAL = 30000; // 30초
        this.init();
    }

    /**
     * 초기화
     */
    init() {
        document.addEventListener('DOMContentLoaded', () => {
            this.initializePage();
        });
    }

    /**
     * 페이지 초기화
     */
    initializePage() {
        // 서버 사이드에서 이미 렌더링된 데이터가 있으면 빈 행만 추가
        const topReturnTbody = document.getElementById('top-return-stocks-tbody');
        if (topReturnTbody && topReturnTbody.children.length > 0) {
            const existingRows = Array.from(topReturnTbody.querySelectorAll('tr'));
            this.ensureMinimumRows(topReturnTbody, 3, existingRows.length);
        }
        
        // 초기 렌더링은 서버 사이드 데이터 사용 (중복 API 호출 방지)
        // 실시간 업데이트만 API 호출
        // 실시간 업데이트 시작 (30초 간격)
        this.startAutoUpdate();
        
        // 페이지 언로드 시 인터벌 정리
        window.addEventListener('beforeunload', () => this.stopAutoUpdate());
    }

    /**
     * 모든 데이터 로드
     */
    async loadAllData() {
        await Promise.all([
            this.loadAccountSummary(),
            this.loadTopReturnStocks(),
            this.loadNearbyRankings(),
            this.loadPortfolioDetails()
        ]);
    }

    /**
     * 실시간 업데이트 시작
     */
    startAutoUpdate() {
        // 기존 인터벌이 있으면 정리
        if (this.updateIntervalId) {
            clearInterval(this.updateIntervalId);
        }
        
        // 30초마다 데이터 갱신
        this.updateIntervalId = setInterval(() => {
            this.loadAllData();
        }, this.UPDATE_INTERVAL);
    }

    /**
     * 실시간 업데이트 중지
     */
    stopAutoUpdate() {
        if (this.updateIntervalId) {
            clearInterval(this.updateIntervalId);
            this.updateIntervalId = null;
        }
    }

    /**
     * 최소 행 수를 보장하는 헬퍼 함수
     */
    ensureMinimumRows(tbody, minRows, currentRows) {
        if (currentRows >= minRows) return;
        
        const rowsToAdd = minRows - currentRows;
        for (let i = 0; i < rowsToAdd; i++) {
            const row = document.createElement('tr');
            row.innerHTML = '<td colspan="5" style="visibility: hidden;">-</td>';
            tbody.appendChild(row);
        }
    }

    /**
     * 로그인 페이지로 리다이렉트
     */
    redirectToLogin() {
        window.location.href = '/login';
    }

    /**
     * API 응답에서 인증 에러 처리
     */
    handleAuthError(response) {
        if (response.status === 401) {
            this.redirectToLogin();
            return true;
        }
        return false;
    }

    /**
     * API 1: 계좌 요약 정보 로드
     */
    async loadAccountSummary() {
        try {
            const response = await fetch('/api/v1/league/account/summary');
            
            if (!response.ok) {
                if (this.handleAuthError(response)) {
                    return;
                }
                throw new Error('계좌 정보를 불러올 수 없습니다.');
            }
            
            const data = await response.json();
            
            // 계좌 총 금액
            const totalAssetEl = document.querySelector('.league__summary-grid .league__summary-box:first-child .league__summary-value');
            if (totalAssetEl) {
                totalAssetEl.textContent = LeaguePageUtils.formatCurrency(data.totalAsset);
            }
            
            // 상위 백분율
            const percentileEl = document.querySelector('.league__summary-grid .league__summary-box:nth-child(2) .league__summary-value');
            if (percentileEl) {
                percentileEl.textContent = data.percentile.toFixed(1) + '%';
            }
            
            // 투자중인 금액
            const stockValueEl = document.querySelector('.league__summary-box[style*="margin-bottom"] .league__summary-value');
            if (stockValueEl) {
                const totalReturn = LeaguePageUtils.formatReturnRate(data.totalReturn);
                stockValueEl.innerHTML = LeaguePageUtils.formatCurrency(data.stockValue) + ' <span class="' + totalReturn.className + '">(' + totalReturn.text + ')</span>';
            }
            
        } catch (error) {
            console.error('계좌 정보 로드 실패:', error);
        }
    }

    /**
     * API 2: 수익률 상위 3개 종목 로드
     */
    async loadTopReturnStocks() {
        try {
            const response = await fetch('/api/v1/league/portfolio/top-returns?limit=3');
            
            if (!response.ok) {
                if (this.handleAuthError(response)) {
                    return;
                }
                throw new Error('상위 종목 정보를 불러올 수 없습니다.');
            }
            
            const data = await response.json();
            const stocks = data.stocks || [];
            
            const tbody = document.getElementById('top-return-stocks-tbody');
            if (!tbody) return;
            
            // 서버 사이드에서 이미 렌더링된 데이터가 있으면 전체 교체
            // 기존 행 제거
            tbody.innerHTML = '';
            
            // 상위 3개 종목 렌더링
            stocks.forEach((stock, index) => {
                const row = document.createElement('tr');
                const changeRateClass = LeaguePageUtils.getChangeRateClass(stock.changeRate);
                
                row.innerHTML = `
                    <td>${index + 1}</td>
                    <td>${stock.stockName || ''}</td>
                    <td>${LeaguePageUtils.formatCurrency(stock.currentPrice)}</td>
                    <td>${LeaguePageUtils.formatCurrency(stock.avgPurchasePrice || 0)}</td>
                    <td><span class="${changeRateClass}">${LeaguePageUtils.formatChangeRate(stock.changeRate)}</span></td>
                `;
                
                tbody.appendChild(row);
            });
            
            // 데이터가 없을 경우
            if (stocks.length === 0) {
                const row = document.createElement('tr');
                row.innerHTML = '<td colspan="5" style="text-align: center; color: #999; padding: 20px 0; font-size: 16px;">보유 종목이 없습니다.</td>';
                tbody.appendChild(row);
            }
            
            // 레이아웃 크기 유지를 위한 빈 행 추가
            this.ensureMinimumRows(tbody, 3, stocks.length === 0 ? 1 : stocks.length);
            
        } catch (error) {
            console.error('상위 종목 정보 로드 실패:', error);
        }
    }

    /**
     * API 3: 주변 랭킹 로드
     */
    async loadNearbyRankings() {
        try {
            const response = await fetch('/api/v1/league/ranking/nearby');
            
            if (!response.ok) {
                if (this.handleAuthError(response)) {
                    return;
                }
                throw new Error('랭킹 정보를 불러올 수 없습니다.');
            }
            
            const data = await response.json();
            const rankings = data.rankings || [];
            
            const rankList = document.getElementById('rankings-list');
            if (!rankList) return;
            
            // 기존 항목 제거
            rankList.innerHTML = '';
            
            // 랭킹 렌더링
            rankings.forEach(ranking => {
                const li = document.createElement('li');
                li.className = 'league__rank-item' + (ranking.isCurrentUser ? ' active' : '');
                
                const rankNumber = String(ranking.rank).padStart(5, '0');
                const returnRateText = ranking.returnRate > 0 ? '+' + ranking.returnRate.toFixed(2) + '%' : ranking.returnRate.toFixed(2) + '%';
                
                li.innerHTML = `
                    <span class="league__rank-number">${rankNumber}</span>
                    <span class="league__rank-name">${ranking.nickname || ''}</span>
                    <span class="league__rank-return">${returnRateText}</span>
                `;
                
                rankList.appendChild(li);
            });
            
            // 데이터가 없을 경우
            if (rankings.length === 0) {
                const li = document.createElement('li');
                li.innerHTML = '<span style="font-size: 12px; color: #999;">랭킹 데이터가 없습니다.</span>';
                rankList.appendChild(li);
            }
            
        } catch (error) {
            console.error('랭킹 정보 로드 실패:', error);
        }
    }

    /**
     * API 4: 전체 포트폴리오 로드
     */
    async loadPortfolioDetails() {
        try {
            const response = await fetch('/api/v1/league/portfolio/details');
            
            if (!response.ok) {
                if (this.handleAuthError(response)) {
                    return;
                }
                throw new Error('포트폴리오 정보를 불러올 수 없습니다.');
            }
            
            const data = await response.json();
            const holdings = data.holdings || [];
            
            const tbody = document.getElementById('portfolio-tbody');
            if (!tbody) return;
            
            // 기존 행 제거
            tbody.innerHTML = '';
            
            // 포트폴리오 렌더링
            holdings.forEach(stock => {
                const row = document.createElement('tr');
                const changeRateClass = LeaguePageUtils.getChangeRateClass(stock.changeRate);
                
                row.innerHTML = `
                    <td>${stock.stockName || ''}</td>
                    <td>${LeaguePageUtils.formatCurrency(stock.currentPrice)}</td>
                    <td><span class="${changeRateClass}">${LeaguePageUtils.formatChangeRate(stock.changeRate)}</span></td>
                `;
                
                tbody.appendChild(row);
            });
            
            // 데이터가 없을 경우
            if (holdings.length === 0) {
                const row = document.createElement('tr');
                row.innerHTML = '<td colspan="3" style="text-align: center; color: #999; padding: 20px 0; font-size: 16px;">보유 종목이 없습니다.</td>';
                tbody.appendChild(row);
            }
            
        } catch (error) {
            console.error('포트폴리오 정보 로드 실패:', error);
        }
    }
}

/**
 * 리그 페이지 유틸리티 클래스
 * 포맷팅 및 헬퍼 함수들
 */
class LeaguePageUtils {
    /**
     * 숫자를 천 단위 콤마로 포맷팅
     */
    static formatNumber(num) {
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    }

    /**
     * 금액을 원화 형식으로 포맷팅
     */
    static formatCurrency(amount) {
        return this.formatNumber(amount) + '원';
    }

    /**
     * 수익률을 퍼센트 형식으로 포맷팅 (색상 클래스 포함)
     */
    static formatReturnRate(rate) {
        const text = formatPriceChange(rate) + '%';
        return { text: text, className: getPriceColorClass(rate) };
    }

    /**
     * 등락률을 퍼센트 형식으로 포맷팅
     */
    static formatChangeRate(rate) {
        return formatPriceChange(rate) + '%';
    }

    /**
     * 등락률에 따른 색상 클래스 반환
     */
    static getChangeRateClass(rate) {
        return getPriceColorClass(rate);
    }
}

// 페이지 초기화
const leaguePage = new LeaguePage();
