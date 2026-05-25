namespace POEPart3
{
    public delegate void HighCalorieNotification(string recipeName);

    public class RecipeManager
    {
        public List<Recipe> Recipes { get; set; }
        public HighCalorieNotification HighCalorieAlert { get; set; }

        public RecipeManager()
        {
            Recipes = new List<Recipe>();
        }

        public void AddRecipe(Recipe recipe)
        {
            Recipes.Add(recipe);
            if (recipe.Ingredients.Sum(i => i.Calories * i.Quantity) > 300)
            {
                HighCalorieAlert?.Invoke(recipe.Name);
            }
        }

        public Recipe SelectRecipe(string name)
        {
            return Recipes.FirstOrDefault(r => r.Name.Equals(name, StringComparison.InvariantCultureIgnoreCase));
        }

        public List<Recipe> GetSortedRecipes()
        {
            return Recipes.OrderBy(r => r.Name).ToList();
        }
    }
}