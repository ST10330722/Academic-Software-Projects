using Microsoft.EntityFrameworkCore.Migrations;

public partial class AddClaimsTable : Migration
{
    protected override void Up(MigrationBuilder migrationBuilder)
    {
        migrationBuilder.CreateTable(
            name: "Claims",
            columns: table => new
            {
                Id = table.Column<int>(type: "int", nullable: false)
                    .Annotation("SqlServer:Identity", "1, 1"),
                LecturerName = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: false),
                HourlyRate = table.Column<decimal>(type: "decimal(18,2)", nullable: false),
                HoursWorked = table.Column<int>(type: "int", nullable: false),
                TotalAmount = table.Column<decimal>(type: "decimal(18,2)", nullable: false),
                Status = table.Column<string>(type: "nvarchar(max)", nullable: true),
                DocumentPath = table.Column<string>(type: "nvarchar(max)", nullable: true),
                DateSubmitted = table.Column<DateTime>(type: "datetime2", nullable: true)
            },
            constraints: table =>
            {
                table.PrimaryKey("PK_Claims", x => x.Id);
            });
    }

    protected override void Down(MigrationBuilder migrationBuilder)
    {
        migrationBuilder.DropTable(
            name: "Claims");
    }
}
