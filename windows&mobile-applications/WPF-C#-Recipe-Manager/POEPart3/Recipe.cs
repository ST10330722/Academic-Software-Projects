using System.Windows;

namespace POEPart3
{
    public class Recipe
    {
        public string Name { get; set; }
        public List<Ingredient> Ingredients { get; set; }
        public List<string> Steps { get; set; }

        public string IngredientsDisplay => string.Join(", ", Ingredients.Select(i => i.Name + " (" + i.Quantity + " " + i.Unit + ")"));

        public Recipe()
        {
            Ingredients = new List<Ingredient>();
            Steps = new List<string>();
        }

        public void GetRecipeDetails()
        {
            Name = Microsoft.VisualBasic.Interaction.InputBox("Enter the recipe name:");

            int numIngredients;
            if (int.TryParse(Microsoft.VisualBasic.Interaction.InputBox("Enter the number of ingredients for this recipe:"), out numIngredients) && numIngredients > 0)
            {
                for (int i = 0; i < numIngredients; i++)
                {
                    Ingredient ingredient = new Ingredient();
                    ingredient.Name = Microsoft.VisualBasic.Interaction.InputBox($"Enter the name of ingredient {i + 1}:");
                    ingredient.Quantity = double.Parse(Microsoft.VisualBasic.Interaction.InputBox($"Enter the quantity of {ingredient.Name}:"));
                    ingredient.Unit = Microsoft.VisualBasic.Interaction.InputBox($"Enter the unit of measurement for {ingredient.Name}:");
                    ingredient.Calories = int.Parse(Microsoft.VisualBasic.Interaction.InputBox($"Enter the number of calories per unit for {ingredient.Name} :", "0"));
                    ingredient.FoodGroup = Microsoft.VisualBasic.Interaction.InputBox($"Enter the food group for {ingredient.Name} :");

                    Ingredients.Add(ingredient);
                }
            }

            bool addingSteps = true;
            while (addingSteps)
            {
                string step = Microsoft.VisualBasic.Interaction.InputBox("Enter a step (or leave blank to finish):");
                if (string.IsNullOrEmpty(step))
                {
                    addingSteps = false;
                }
                else
                {
                    Steps.Add(step);
                }
            }
        }

        public void DisplayRecipe()
        {
            string ingredientsDisplay = string.Join(Environment.NewLine, Ingredients.Select(i => $"{i.Quantity} {i.Unit} of {i.Name} ({i.Calories} calories, {i.FoodGroup} food group)"));
            string stepsDisplay = string.Join(Environment.NewLine, Steps.Select((s, index) => $"{index + 1}. {s}"));

            MessageBox.Show($"Recipe: {Name}\n\nIngredients:\n{ingredientsDisplay}\n\nSteps:\n{stepsDisplay}");
        }

        public void ResetQuantities()
        {
            foreach (var ingredient in Ingredients)
            {
                ingredient.Quantity = 1.0;
            }
        }

        public void ScaleRecipe(double factor)
        {
            foreach (var ingredient in Ingredients)
            {
                ingredient.Quantity *= factor;
            }

            DisplayRecipe();
        }
    }
}