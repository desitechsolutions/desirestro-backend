package com.dts.restro.menu.mapper;

import com.dts.restro.menu.dto.CategoryDTO;
import com.dts.restro.menu.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toDTO(Category category);

    @Mapping(target = "items", ignore = true)
    Category toEntity(CategoryDTO dto);
}
