package com.dts.restro.menu.mapper;

import com.dts.restro.menu.dto.MenuItemIngredientDTO;
import com.dts.restro.menu.entity.MenuItemIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuItemIngredientMapper {

    @Mapping(source = "ingredient.id", target = "ingredientId")
    MenuItemIngredientDTO toDTO(MenuItemIngredient entity);

    @Mapping(source = "ingredientId", target = "ingredient.id")
    @Mapping(target = "menuItem", ignore = true)
    MenuItemIngredient toEntity(MenuItemIngredientDTO dto);

    List<MenuItemIngredientDTO> toDTOList(List<MenuItemIngredient> entities);

    List<MenuItemIngredient> toEntityList(List<MenuItemIngredientDTO> dtos);
}
