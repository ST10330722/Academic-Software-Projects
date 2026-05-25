using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using Azure.Storage.Blobs;
using Azure.Data.Tables;
using Azure.Storage.Queues;
using Azure.Storage.Files.Shares;
using Azure;
using System.Threading.Tasks;
using System.IO;
using System;

namespace ABC_Retail.Pages
{
    public class IndexModel : PageModel
    {
        public void OnGet()
        {
        }
    }
}