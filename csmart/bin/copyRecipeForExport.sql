-- Copy the named Recipe for export.
-- Creates the tempcopy db again, and leaves it there.
-- Used by export-recipe.sh

CREATE DATABASE tempcopy;

-- v4_lib_mod_recipe
DROP TABLE IF EXISTS tempcopy.v4_lib_mod_recipe;

CREATE TABLE tempcopy.v4_lib_mod_recipe AS
  SELECT DISTINCT
    CONCAT(MOD_RECIPE_LIB_ID, NAME) AS MOD_RECIPE_LIB_ID,
    CONCAT(NAME, '-cpy') AS NAME,
    JAVA_CLASS,
    DESCRIPTION
  FROM
    v4_lib_mod_recipe
  WHERE
    NAME = ':recipeName';

-- v4_lib_mod_recipe_arg
DROP TABLE IF EXISTS tempcopy.v4_lib_mod_recipe_arg;

CREATE TABLE tempcopy.v4_lib_mod_recipe_arg AS
  SELECT DISTINCT
    CONCAT(AA.MOD_RECIPE_LIB_ID, AT.NAME) AS MOD_RECIPE_LIB_ID,
    AA.ARG_NAME,
    AA.ARG_ORDER,
    AA.ARG_VALUE
  FROM
    v4_lib_mod_recipe_arg AA,
    v4_lib_mod_recipe AT
  WHERE
    AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName';

