package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.SearchDAO;
import com.dongyang.mock.model.DTO.StockDTO;

import java.util.List;

public class SearchService {

    private final SearchDAO searchServiceDAO = new SearchDAO();

    // 검색어를 받아 DAO에 위임
    public List<StockDTO> searchStock(String keyword) {
        return searchServiceDAO.search(keyword);
    }
}
