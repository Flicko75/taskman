package com.flicko.TaskMan.utils;

import com.flicko.TaskMan.DTOs.PageResponse;
import org.springframework.data.domain.Page;

public class PageMapper {

    private PageMapper(){

    }

    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

}
