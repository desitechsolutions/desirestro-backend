package com.dts.restro.mapper;

import com.dts.restro.dto.menu.CategoryDTO;
import com.dts.restro.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toDTO(Category category);
    Category toEntity(CategoryDTO dto);
}
