package nstv.bluetoothmagic.data.local

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import nstv.bluetoothmagic.R
import nstv.bluetoothmagic.ui.theme.IngredientColor

data class Ingredient(
    val id: Int,
    val name: String,
    @DrawableRes val resource: Int,
    val color: Color,
    val count: Int = 0,
    val isMainIngredient: Boolean = false,
)

fun Ingredient.toIngredientCount() =
    IngredientCount(
        id = id,
        count = count,
        isMainIngredient = isMainIngredient,
    )

fun IngredientCount.toIngredient() =
    IngredientCombinations.list.first { id == it.id }.copy(
        count = count,
        isMainIngredient = isMainIngredient,
    )

object IngredientCombinations {
    val bayBolete = Ingredient(
        id = 1,
        name = "Bay Bolete",
        resource = R.drawable.bay_bolete,
        color = IngredientColor.Blue,
    )

    val champignon = Ingredient(
        id = 2,
        name = "Champignon",
        resource = R.drawable.champignon,
        color = IngredientColor.Green,
    )

    val chanterelle = Ingredient(
        id = 3,
        name = "Chanterelle",
        resource = R.drawable.chanterelle,
        color = IngredientColor.Magenta,
    )

    val honeyMushroom = Ingredient(
        id = 4,
        name = "Honey Mushroom",
        resource = R.drawable.honey_mushroom,
        color = IngredientColor.Yellow,
    )

    val inkyCap = Ingredient(
        id = 5,
        name = "Inky Cap",
        resource = R.drawable.inky_cap,
        color = IngredientColor.Purple,
    )

    val micaCap = Ingredient(
        id = 6,
        name = "Mica Cap",
        resource = R.drawable.mica_cap,
        color = IngredientColor.Orange,
    )

    val pennyBun = Ingredient(
        id = 7,
        name = "Penny Bun",
        resource = R.drawable.penny_bun,
        color = IngredientColor.Red,
    )

    val shaggyMane = Ingredient(
        id = 8,
        name = "Shaggy Mane",
        resource = R.drawable.shaggy_mane,
        color = IngredientColor.Cyan,
    )

    val shaggyParasol = Ingredient(
        id = 9,
        name = "Shaggy Parasol",
        resource = R.drawable.shaggy_parasol,
        color = IngredientColor.Pink,
    )

    val shiitake = Ingredient(
        id = 10,
        name = "Shiitake",
        resource = R.drawable.shiitake,
        color = IngredientColor.PaleGreen,
    )

    val list = listOf(
        bayBolete,
        champignon,
        chanterelle,
        honeyMushroom,
        inkyCap,
        micaCap,
        pennyBun,
        shaggyMane,
        shaggyParasol,
        shiitake,
    )

    fun getRandom() = list.random()
}