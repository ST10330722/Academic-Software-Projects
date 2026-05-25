using Microsoft.AspNetCore.Identity;

namespace AgriEnergyConnect.Models
{
    public class ApplicationUser : IdentityUser
    {
        public string FullName { get; set; }
    }
}
