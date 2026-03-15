package com.dts.restro.mapper;

import com.dts.restro.dto.menu.MenuItemDTO;
import com.dts.restro.entity.MenuItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MenuItemIngredientMapper.class})
public interface MenuItemMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    MenuItemDTO toDTO(MenuItem entity);

    @Mapping(source = "categoryId", target = "category.id")
    MenuItem toEntity(MenuItemDTO dto);

    List<MenuItemDTO> toDTOList(List<MenuItem> entities);
}
