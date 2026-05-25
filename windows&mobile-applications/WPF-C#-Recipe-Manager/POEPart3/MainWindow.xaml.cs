using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace POEPart3
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        private RecipeManager recipeBook = new RecipeManager();
        private Recipe currentRecipe = null;

        public MainWindow()
        {
            InitializeComponent();
            recipeBook.HighCalorieAlert += Recipe_OnHighCalorie;
        }

        private void EnterRecipes_Click(object sender, RoutedEventArgs e)
        {
            int numRecipes;
            if (int.TryParse(Microsoft.VisualBasic.Interaction.InputBox("Enter the number of recipes you want to add:"), out numRecipes) && numRecipes > 0)
            {
                for (int i = 0; i < numRecipes; i++)
                {
                    Recipe recipe = new Recipe();
                    recipe.GetRecipeDetails();
                    recipeBook.AddRecipe(recipe);
                    MessageBox.Show($"Recipe '{recipe.Name}' added successfully!");
                }

                DisplayRecipes_Click(sender, e);
            }
            else
            {
                MessageBox.Show("Invalid number of recipes. Please enter a positive integer.");
            }
        }

        private void DisplayRecipes_Click(object sender, RoutedEventArgs e)
        {
            RecipeListView.ItemsSource = recipeBook.GetSortedRecipes();
        }

        private void SelectRecipe_Click(object sender, RoutedEventArgs e)
        {
            string name = Microsoft.VisualBasic.Interaction.InputBox("Enter the name of the recipe you want to display:");
            currentRecipe = recipeBook.SelectRecipe(name);
            if (currentRecipe != null)
            {
                currentRecipe.DisplayRecipe();
            }
        }

        private void ResetQuantities_Click(object sender, RoutedEventArgs e)
        {
            if (currentRecipe != null)
            {
                currentRecipe.ResetQuantities();
                MessageBox.Show("Recipe quantities reset.");
            }
            else
            {
                MessageBox.Show("No recipe currently selected to reset.");
            }
        }

        private void ScaleRecipe_Click(object sender, RoutedEventArgs e)
        {
            if (currentRecipe != null)
            {
                double factor;
                if (double.TryParse(Microsoft.VisualBasic.Interaction.InputBox("Enter the scaling factor (e.g., 0.5, 2, 3):"), out factor) && factor > 0)
                {
                    currentRecipe.ScaleRecipe(factor);
                }
                else
                {
                    MessageBox.Show("Invalid scaling factor. Please enter a positive number.");
                }
            }
            else
            {
                MessageBox.Show("No recipe currently selected to scale.");
            }
        }

        private void SetHighCalorieNotification_Click(object sender, RoutedEventArgs e)
        {
            string notificationChoice = Microsoft.VisualBasic.Interaction.InputBox("Set high-calorie notification (choose 1 or 2):\n1. In-app notification (default)\n2. Custom notification (provide method name)");

            switch (notificationChoice)
            {
                case "1":
                    MessageBox.Show("High-calorie notification set to in-app message.");
                    break;
                case "2":
                    string methodName = Microsoft.VisualBasic.Interaction.InputBox("Enter the name of your custom notification method:");
                    MessageBox.Show("High-calorie notification set to custom method (if found).");
                    break;
                default:
                    MessageBox.Show("Invalid choice. High-calorie notification remains unchanged.");
                    break;
            }
        }

        private void FilterRecipes_Click(object sender, RoutedEventArgs e)
        {
            FilterWindow filterWindow = new FilterWindow(recipeBook);
            filterWindow.ShowDialog();
        }

        private void Exit_Click(object sender, RoutedEventArgs e)
        {
            Application.Current.Shutdown();
        }

        private void Recipe_OnHighCalorie(string recipeName)
        {
            MessageBox.Show($"\nWarning: The recipe '{recipeName}' exceeds 300 calories!");
        }
    }
}