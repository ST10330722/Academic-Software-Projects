using Azure.Data.Tables;
using Azure.Storage.Blobs;
using Azure.Storage.Files.Shares;
using Azure.Storage.Queues;
using Azure;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace ABC_Retail.Pages
{
    public class OrdersModel : PageModel
    {
        private readonly BlobServiceClient _blobServiceClient;
        private readonly TableServiceClient _tableServiceClient;
        private readonly QueueServiceClient _queueServiceClient;
        private readonly ShareServiceClient _shareServiceClient;

        public OrdersModel(BlobServiceClient blobServiceClient, TableServiceClient tableServiceClient, QueueServiceClient queueServiceClient, ShareServiceClient shareServiceClient)
        {
            _blobServiceClient = blobServiceClient;
            _tableServiceClient = tableServiceClient;
            _queueServiceClient = queueServiceClient;
            _shareServiceClient = shareServiceClient;
        }

        public async Task<JsonResult> OnPostSubmitProfileAsync(string customerName, string productName)
        {
            try
            {
                var tableClient = _tableServiceClient.GetTableClient("Profiles");
                await tableClient.CreateIfNotExistsAsync();

                var entity = new TableEntity("Profiles", customerName)
                {
                    { "ProductName", productName }
                };

                await tableClient.AddEntityAsync(entity);
                return new JsonResult(new { success = true, message = "Profile successfully submitted." });
            }
            catch (Exception ex)
            {
                return new JsonResult(new { success = false, message = $"Error: {ex.Message}" });
            }
        }

        public async Task<JsonResult> OnPostUploadImageAsync(IFormFile imageFile)
        {
            try
            {
                var containerClient = _blobServiceClient.GetBlobContainerClient("images");
                await containerClient.CreateIfNotExistsAsync();

                var blobClient = containerClient.GetBlobClient(imageFile.FileName);
                using (var stream = imageFile.OpenReadStream())
                {
                    await blobClient.UploadAsync(stream, overwrite: true);
                }

                return new JsonResult(new { success = true, message = "Image successfully uploaded." });
            }
            catch (Exception ex)
            {
                return new JsonResult(new { success = false, message = $"Error: {ex.Message}" });
            }
        }

        public async Task<JsonResult> OnPostProcessOrderAsync(string orderDetails)
        {
            try
            {
                var queueClient = _queueServiceClient.GetQueueClient("orders");
                await queueClient.CreateIfNotExistsAsync();
                await queueClient.SendMessageAsync(orderDetails);

                return new JsonResult(new { success = true, message = "Order successfully processed." });
            }
            catch (Exception ex)
            {
                return new JsonResult(new { success = false, message = $"Error: {ex.Message}" });
            }
        }

        public async Task<JsonResult> OnPostUploadContractAsync(IFormFile contractFile)
        {
            try
            {
                var shareClient = _shareServiceClient.GetShareClient("contracts");
                await shareClient.CreateIfNotExistsAsync();

                var rootDir = shareClient.GetRootDirectoryClient();
                var fileClient = rootDir.GetFileClient(contractFile.FileName);

                using (var stream = contractFile.OpenReadStream())
                {
                    await fileClient.CreateAsync(stream.Length);
                    await fileClient.UploadRangeAsync(new HttpRange(0, stream.Length), stream);
                }

                return new JsonResult(new { success = true, message = "Contract successfully uploaded." });
            }
            catch (Exception ex)
            {
                return new JsonResult(new { success = false, message = $"Error: {ex.Message}" });
            }
        }
    }
}
    