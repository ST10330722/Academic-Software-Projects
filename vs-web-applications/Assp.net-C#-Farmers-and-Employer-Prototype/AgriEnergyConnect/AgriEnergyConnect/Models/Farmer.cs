using System.ComponentModel.DataAnnotations;

namespace AgriEnergyConnect.Models
{
    public class Farmer
    {
        public int Id { get; set; }
        [Required]
        public string Name { get; set; }
        [Required]
        public string Location { get; set; }
        [Required]
        [Display(Name = "Contact Info")]
        public string ContactInfo { get; set; }
    }
}
