package com.dts.restro.mapper;

import com.dts.restro.dto.menu.CategoryDTO;
import com.dts.restro.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toDTO(Category category);

    @Mapping(target = "items", ignore = true)
    Category toEntity(CategoryDTO dto);
}
