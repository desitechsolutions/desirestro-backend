package com.dts.restro.mapper;

import com.dts.restro.dto.menu.MenuItemIngredientDTO;
import com.dts.restro.entity.MenuItemIngredient;
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
