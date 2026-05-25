using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;
using AgriEnergyConnect.Models;
namespace AgriEnergyConnect.Data
{
    public class ApplicationDbContext : IdentityDbContext<ApplicationUser>
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options) : base(options) { }

        public DbSet<Farmer> Farmers { get; set; }
        public DbSet<Product> Products { get; set; }
    }
}
