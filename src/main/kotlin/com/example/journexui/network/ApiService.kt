package com.example.journexui.network

import com.example.journexui.model.*
import retrofit2.http.*

interface ApiService {
 @POST("auth/register") suspend fun register(@Body request:RegisterRequest):Long
 @POST("auth/login") suspend fun login(@Body request:LoginRequest):String
 @POST("auth/logout") suspend fun logout(@Header("Authorization") authHeader:String):String
 @GET("users/me") suspend fun me(@Header("Authorization") auth:String):UserDto
 @PUT("users/update") suspend fun updateProfile(@Header("Authorization") auth:String,@Body request:UpdateProfileDto):UserDto
 @POST("users/change-password") suspend fun changePassword(@Header("Authorization") auth:String,@Body request:ChangePasswordDto)

 @GET("strategy/find-all") suspend fun strategies(@Header("Authorization") auth:String,@Query("page") page:Int,@Query("size") size:Int,@Query("sortBy") sortBy:String="id",@Query("direction") direction:String="DESC"):ApiPage<StrategyDto>
 @GET("strategy/find-all-deleted") suspend fun deletedStrategies(@Header("Authorization") auth:String,@Query("page") page:Int,@Query("size") size:Int,@Query("sortBy") sortBy:String="id",@Query("direction") direction:String="DESC"):ApiPage<StrategyDto>
 @GET("strategy/{id}") suspend fun strategy(@Header("Authorization") auth:String,@Path("id") id:Long):StrategyDto
 @GET("strategy/public/{address}") suspend fun publicStrategy(@Path("address") address:String):StrategyDto
 @POST("strategy/save") suspend fun saveStrategy(@Header("Authorization") auth:String,@Body dto:StrategyDto):Long
 @PUT("strategy/update/{id}") suspend fun updateStrategy(@Header("Authorization") auth:String,@Path("id") id:Long,@Body dto:StrategyDto):Long
 @DELETE("strategy/delete/{id}") suspend fun deleteStrategy(@Header("Authorization") auth:String,@Path("id") id:Long)
 @PUT("strategy/restore/{id}") suspend fun restoreStrategy(@Header("Authorization") auth:String,@Path("id") id:Long)

 @GET("checklist/find-all") suspend fun checklists(@Header("Authorization") auth:String,@Query("page") page:Int,@Query("size") size:Int,@Query("sortBy") sortBy:String="id",@Query("direction") direction:String="DESC"):ApiPage<ChecklistDto>
 @GET("checklist/find-all-deleted") suspend fun deletedChecklists(@Header("Authorization") auth:String,@Query("page") page:Int,@Query("size") size:Int,@Query("sortBy") sortBy:String="id",@Query("direction") direction:String="DESC"):ApiPage<ChecklistDto>
 @GET("checklist/find-all-actives") suspend fun activeChecklists(@Header("Authorization") auth:String,@Query("page") page:Int,@Query("size") size:Int,@Query("sortBy") sortBy:String="id",@Query("direction") direction:String="DESC"):ApiPage<ChecklistDto>
 @GET("checklist/find-by-strategy/{strategyId}") suspend fun checklistsByStrategy(@Header("Authorization") auth:String,@Path("strategyId") strategyId:Long,@Query("page") page:Int,@Query("size") size:Int,@Query("sortBy") sortBy:String="id",@Query("direction") direction:String="DESC"):ApiPage<ChecklistDto>
 @POST("checklist/save") suspend fun saveChecklist(@Header("Authorization") auth:String,@Body dto:ChecklistDto):Long
 @PUT("checklist/update/{id}") suspend fun updateChecklist(@Header("Authorization") auth:String,@Path("id") id:Long,@Body dto:ChecklistDto):Long
 @DELETE("checklist/delete/{id}") suspend fun deleteChecklist(@Header("Authorization") auth:String,@Path("id") id:Long)
 @PUT("checklist/restore/{id}") suspend fun restoreChecklist(@Header("Authorization") auth:String,@Path("id") id:Long)

 @GET("checklist-items/checklist/{checklistId}") suspend fun checklistItems(@Header("Authorization") auth:String,@Path("checklistId") checklistId:Long,@Query("page") page:Int,@Query("size") size:Int,@Query("sortBy") sortBy:String="orderIndex",@Query("direction") direction:String="ASC"):ApiPage<ChecklistItemDto>
 @GET("checklist-items/{itemId}") suspend fun checklistItem(@Header("Authorization") auth:String,@Path("itemId") itemId:Long):ChecklistItemDto
 @POST("checklist-items/add-question") suspend fun addQuestion(@Header("Authorization") auth:String,@Body dto:ChecklistItemDto):Long
 @PUT("checklist-items/{itemId}") suspend fun updateItem(@Header("Authorization") auth:String,@Path("itemId") id:Long,@Body dto:ChecklistItemDto):Long
 @DELETE("checklist-items/{itemId}") suspend fun deleteItem(@Header("Authorization") auth:String,@Path("itemId") id:Long)
 @POST("checklist-items/{itemId}/restore") suspend fun restoreItem(@Header("Authorization") auth:String,@Path("itemId") id:Long,@Query("checklistId") checklistId:Long)
 @PUT("checklist-items/{itemId}/move") suspend fun moveItem(@Header("Authorization") auth:String,@Path("itemId") id:Long,@Query("newOrder") order:Long)
 @POST("checklist-items/answer") suspend fun answerItem(@Header("Authorization") auth:String,@Body dto:ChecklistItemAnswerDto):Long

 @GET("trades") suspend fun trades(@Header("Authorization") auth:String,@Query("page") page:Int,@Query("size") size:Int,@Query("sortBy") sortBy:String="id",@Query("direction") direction:String="DESC"):ApiPage<TradeDto>
 @GET("trades/trash") suspend fun deletedTrades(@Header("Authorization") auth:String,@Query("page") page:Int,@Query("size") size:Int,@Query("sortBy") sortBy:String="id",@Query("direction") direction:String="DESC"):ApiPage<TradeDto>
 @GET("trades/status") suspend fun tradesByStatus(@Header("Authorization") auth:String,@Query("status") status:String,@Query("page") page:Int,@Query("size") size:Int,@Query("sortBy") sortBy:String="id",@Query("direction") direction:String="DESC"):ApiPage<TradeDto>
 @GET("trades/strategy/{strategyId}") suspend fun tradesByStrategy(@Header("Authorization") auth:String,@Path("strategyId") strategyId:Long,@Query("page") page:Int,@Query("size") size:Int,@Query("sortBy") sortBy:String="id",@Query("direction") direction:String="DESC"):ApiPage<TradeDto>
 @GET("trades/{tradeId}") suspend fun trade(@Header("Authorization") auth:String,@Path("tradeId") id:Long):TradeDto
 @POST("trades") suspend fun openTrade(@Header("Authorization") auth:String,@Body dto:TradeOpenRequestDto):Long
 @POST("trades/{tradeId}/close") suspend fun closeTrade(@Header("Authorization") auth:String,@Path("tradeId") id:Long,@Body dto:TradeCloseRequestDto)
 @PUT("trades/{tradeId}/journal") suspend fun updateJournal(@Header("Authorization") auth:String,@Path("tradeId") id:Long,@Body dto:TradeJournalUpdateDto)
 @PATCH("trades/{tradeId}/risk") suspend fun updateRisk(@Header("Authorization") auth:String,@Path("tradeId") id:Long,@Body dto:TradeRiskUpdateDto)
 @DELETE("trades/{tradeId}") suspend fun deleteTrade(@Header("Authorization") auth:String,@Path("tradeId") id:Long)
 @POST("trades/{tradeId}/restore") suspend fun restoreTrade(@Header("Authorization") auth:String,@Path("tradeId") id:Long)
}
