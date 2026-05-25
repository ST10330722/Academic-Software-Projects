using Azure.Storage.Blobs;
using Azure.Data.Tables;
using Azure.Storage.Queues;
using Azure.Storage.Files.Shares;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddRazorPages();

// Register Azure service clients with connection strings from configuration
builder.Services.AddSingleton(new BlobServiceClient(builder.Configuration.GetConnectionString("AzureBlobStorage")));
builder.Services.AddSingleton(new TableServiceClient(builder.Configuration.GetConnectionString("AzureTableStorage")));
builder.Services.AddSingleton(new QueueServiceClient(builder.Configuration.GetConnectionString("AzureQueueStorage")));
builder.Services.AddSingleton(new ShareServiceClient(builder.Configuration.GetConnectionString("AzureFileStorage")));

var app = builder.Build();

// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();

app.UseAuthorization();

app.MapRazorPages();

app.Run();