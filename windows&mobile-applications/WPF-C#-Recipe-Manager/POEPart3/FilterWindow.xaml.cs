using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;

namespace POEPart3
{
    /// <summary>
    /// Interaction logic for FilterWindow.xaml
    /// </summary>
    public partial class FilterWindow : Window
    {
        private RecipeManager recipeBook;

        public FilterWindow(RecipeManager recipeBook)
        {
            InitializeComponent();
            this.recipeBook = recipeBook;
        }

        private void ApplyFilter_Click(object sender, RoutedEventArgs e)
        {
            string ingredient = IngredientTextBox.Text.ToLower();
            string foodGroup = FoodGroupTextBox.Text.ToLower();
            int maxCalories = 0;
            int.TryParse(MaxCaloriesTextBox.Text, out maxCalories);

            var filteredRecipes = recipeBook.Recipes.Where(r =>
                (string.IsNullOrEmpty(ingredient) || r.Ingredients.Any(i => i.Name.ToLower().Contains(ingredient))) &&
                (string.IsNullOrEmpty(foodGroup) || r.Ingredients.Any(i => i.FoodGroup.ToLower().Contains(foodGroup))) &&
                (maxCalories == 0 || r.Ingredients.Sum(i => i.Calories * i.Quantity) <= maxCalories)).ToList();

            MainWindow mainWindow = (MainWindow)Application.Current.MainWindow;
            mainWindow.RecipeListView.ItemsSource = filteredRecipes;

            MessageBox.Show("Filter applied.");
            this.Close();
        }

        private void Close_Click(object sender, RoutedEventArgs e)
        {
            this.Close();
        }
    }
}