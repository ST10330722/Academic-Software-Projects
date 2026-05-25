using System.ComponentModel.DataAnnotations;

namespace AgriEnergyConnect.Models
{
    public class Product
    {
        public int Id { get; set; }
        [Required]
        public string Name { get; set; }
        [Required]
        public string Category { get; set; }
        [Required]
        public DateTime ProductionDate { get; set; }
        [Required]
        public int FarmerId { get; set; }
        public Farmer Farmer { get; set; }
    }
}
